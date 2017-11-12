/*
 * Copyright 2011, Scott Douglass <scott@swdouglass.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either VERSION 3 of the License, or
 * (at your option) any later VERSION.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * on the World Wide Web for more details:
 * http://www.fsf.org/licensing/licenses/gpl.txt
 */
package com.jmstoolkit.pipeline;

import com.jmstoolkit.JTKException;
import java.util.logging.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import com.jmstoolkit.Settings;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.logging.Level;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;

/**
 * This class manages any number of {@link Plugin} implementations. It is
 * responsible for instantiating, persisting and destroying the implementations.
 *
 * @author Scott Douglass
 */
@ManagedResource(objectName = "jmstoolkit:name=PipelineManager",
description = "Pipeline/Plugin Manager Service")
public class Pipeline extends AbstractPipeline {

  /** */
  private static final Logger LOG = Logger.getLogger(Pipeline.class.getName());
  /** */
  private static final String VERSION = "1";

  /**
   *
   * @param args the command line arguments
   */
  public static void main(final String[] args) {
    // Have to use System.out as logging is configured by Spring
    // application context.
    try {
      Settings.loadSystemSettings("jndi.properties");
      Settings.loadSystemSettings("app.properties");
    } catch (JTKException ex) {
      System.out.println("Failed to load application settings");
      System.out.println(JTKException.formatException(ex));
      System.exit(1);
    }

    final ClassPathXmlApplicationContext applicationContext =
      new ClassPathXmlApplicationContext(
      new String[]{"/logging-context.xml",
        "/infrastructure-context.xml",
        "/mdb-context.xml",
        "/jmx-context.xml"});
    applicationContext.start();

    final DefaultMessageListenerContainer dmlc =
      (DefaultMessageListenerContainer) applicationContext.getBean(
      "listenerContainer");
    if (dmlc != null) {
      dmlc.start();
      final Pipeline pipeline =
        (Pipeline) applicationContext.getBean("pipelineService");
      // enable access to the original application context
      pipeline.setApplicationContext(applicationContext);

      // Insure that the Pipeline loads configuration files AFTER
      // the listenerContainer is running.
      while (!dmlc.isRunning()) {
        try {
          Thread.sleep(100);
        } catch (InterruptedException ex) {
        }
      }
      pipeline.loadPlugins();
      pipeline.sendProperties();

      // Keep thread alive
      while (true) {
        try {
          Thread.sleep(1000);
        } catch (InterruptedException ex) {
        }
      }
    }
  }

  /**
   * Recieves JMS message and decides what to do.
   *   <ul><li>Create a new Plugin task
   *   <li>Reconfigure and existing Plugin task
   *   <li>Destroy an existing Plugin task
   *  </ul>
   * @param message Incoming JMS Message
   */
  @Override
  public final void onMessage(final Message message) {
    if (message instanceof TextMessage) {
      Plugin plugin;
      setMessageCount((Integer) getMessageCount() + 1);
      try {
        final String xml = ((TextMessage) message).getText();
        final Document doc = getAction(xml);
        final String name = trim(doc.valueOf("//plugin/name"));
        final String action = trim(doc.valueOf("//plugin/action"));
        if (ACTION_NEW.equalsIgnoreCase(action)) {
          plugin = getPlugin(doc);
          plugin.init(); // starts the MessageListener
          getPlugins().put(name, plugin);
          //Save file
          saveConfigMessage(name, xml);
        } else if (ACTION_STOP.equalsIgnoreCase(action)) {
          plugin = getPlugins().get(name);
          plugin.stop(); // stops the MessageListener
          getPlugins().remove(name);
          removeConfigMessage(name);
        } else if (ACTION_UPDATE.equalsIgnoreCase(action)) {
          // stop
          plugin = getPlugins().get(name);
          plugin.stop();
          getPlugins().remove(name);
          // start
          plugin = getPlugin(doc);
          plugin.init(); // starts the MessageListener
          getPlugins().put(name, plugin);
          // save, but change update to new
          doc.selectSingleNode("//plugin/action").setText(ACTION_NEW);
          saveConfigMessage(name, doc.asXML());
        } else {
          throw new PipelineException("Unknown action requested: " + action);
        }
      } catch (JMSException ex) {
        LOG.log(Level.SEVERE, "Failed to get text from message", ex);
      } catch (DocumentException ex) {
        LOG.log(Level.SEVERE, "Could not parse action message", ex);
      } catch (PipelineException e) {
        LOG.log(Level.SEVERE, "Pipeline is clogged", e);
      } catch (IOException e) {
        LOG.log(Level.WARNING, "Failed to persist action message", e);
      }
    }
  }

  /**
   * Determine what action the received message contains, validate that and
   * perform it. Validation is done using the JDK's Xerces implementation
   * against a DTD. The DTD should be in the user.dir location!
   *
   * @param xml The XML from the <code>Message</code>
   * @return A dom4j <code>Document</code> for further processing.
   * @throws DocumentException If the XML can not be parsed and validated.
   * @throws PipelineException If the XML contains an invalid action.
   */
  private Document getAction(final String xml) throws DocumentException,
    PipelineException {
    System.out.println("############################################");
    System.out.println("Action message received:");
    System.out.println(xml);
    System.out.println("############################################");

    // Below will all bomb with DocumentException
    // enable validation
    final SAXReader saxReader = new SAXReader(isValidated());
    // Validation is done using the JDK's bundled xerces implementation
    // against a DTD. The DTD should be in the user.dir location!
    final Document doc = saxReader.read(new StringReader(xml));
    final String name = trim(doc.valueOf("//plugin/name"));
    final String action = trim(doc.valueOf("//plugin/action"));
    final String type = trim(doc.valueOf("//plugin/type"));
    final String xversion = trim(doc.valueOf("//plugin/version"));

    LOG.log(Level.INFO,
    "Action message received: {0}, for service name: {1} and service type: {2}",
      new Object[]{action, name, type});

    if (!VERSION.equalsIgnoreCase(xversion)) {
      throw new PipelineException("Action version: " + xversion
        + " does not match Pipeline version: " + VERSION);
    }

    if ("new".equalsIgnoreCase(action)) {
      if (getPlugins().containsKey(name)) {
        throw new PipelineException("Can't create new Plugin name: "
          + name + ". Plugin with same name already exists.");
      }
    } else if ("update".equalsIgnoreCase(action)) {
      if (!getPlugins().containsKey(name)) {
        throw new PipelineException("Can't update Plugin: "
          + name + ". No Plugin with that name found.");
      }
    } else if ("update".equalsIgnoreCase(action)) {
      if (!getPlugins().containsKey(name)) {
        throw new PipelineException("Can't stop Plugin: " + name
          + ". No Plugin with that name found.");
      }
    }
    return doc;
  }

  /**
   * Publish the <code>Pipeline</code> system properties to a
   * JMS <code>Destination</code>.
   */
  public final void sendProperties() {
    try {
      final ByteArrayOutputStream props = new ByteArrayOutputStream();
      System.getProperties().storeToXML(props, "Pipeline");
      getJmsTemplate().convertAndSend(props.toString());
      LOG.info(props.toString());
    } catch (IOException ex) {
      System.out.println("Failed to read System properteis: " + ex);
    }
  }

  /**
   * Get the names of all the <code>Plugin</code> implementation managed
   * by this <code>Pipeline</code>.
   *
   * @return Comma separated list of <code>Plugin</code> names.
   */
  @ManagedAttribute
  public final String getPluginNames() {
    String result = "";
    if (getPlugins().size() > 0) {
      final StringBuilder pluginInfo = new StringBuilder();
      for (String s : getPlugins().keySet()) {
        pluginInfo.append(s);
        pluginInfo.append(",");
      }
      final String tempresult = pluginInfo.toString();
      result = tempresult.substring(0, tempresult.lastIndexOf(','));
    }
    return result;
  }

  /**
   *
   * @return the number of plugins
   */
  @ManagedAttribute
  public final Integer getPluginCount() {
    return this.getPlugins().size();
  }

  /**
   *
   * @param name of the plugin
   * @return the plugin information
   */
  @ManagedOperation
  public final String getPluginInfo(final String name) {
    return (getPlugins().containsKey(name))
      ? getPlugins().get(name).getInfo() : "";
  }

  /**
   *
   * @param name of the plugin
   * @return result
   */
  @ManagedOperation
  public final Integer killPlugin(final String name) {
    Integer result = 1;
    if (this.getPlugins().containsKey(name)) {
      final Plugin plugin = this.getPlugins().get(name);
      plugin.stop();
      this.getPlugins().remove(name);
      //plugin = null;
      result = 0;
      LOG.log(Level.INFO, "Killed Plugin: {1}", name);
    }
    return result;
  }

  /**
   * @return the VERSION
   */
  @ManagedAttribute
  public final String getVersion() {
    return VERSION;
  }

  /**
   *
   * @param name of the plugin
   * @return number operations done by plugin
   */
  @ManagedOperation
  public final Integer getPluginOperationCount(final String name) {
    return (
      getPlugins().containsKey(name))
      ? getPlugins().get(name).getOperationCount() : 0;
  }
}
