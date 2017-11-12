/*
 * Copyright 2011, Scott Douglass <scott@swdouglass.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * on the World Wide Web for more details:
 * http://www.fsf.org/licensing/licenses/gpl.txt
 */
package com.jmstoolkit.pipeline;

import com.jmstoolkit.logging.JTKLogging;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.MessageListener;
import org.dom4j.Document;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jndi.JndiTemplate;

/**
 *
 * @author scott
 */
public abstract class AbstractPipeline implements MessageListener {

  /**
   *
   */
  public static final Logger CONSOLE
    = Logger.getLogger(JTKLogging.CONSOLE_LOGGER_NAME);
  /**
   *    */
  public static final String ACTION_NEW = "new";
  /**
   *    */
  public static final String ACTION_STOP = "stop";
  /**
   *    */
  public static final String ACTION_UPDATE = "update";
  /**
   *    */
  private ClassPathXmlApplicationContext applicationContext;
  /**
   *    */
  private final Map<String, Plugin> plugins
    = new ConcurrentHashMap<>();
  /**
   *    */
  private JndiTemplate jndiTemplate;
  /**
   *    */
  private JmsTemplate jmsTemplate;
  /**
   *    */
  private ConnectionFactory connectionFactory;
  /**
   *    */
  private Destination configTopic;
  /**
   *    */
  public static final String FILE_SEPARATOR
    = System.getProperty("file.separator");
  /**
   *    */
  public static final String USER_DIR = System.getProperty("user.dir");
  /**
   *    */
  private boolean validated = true;
  /**
   *    */
  private boolean persisted = true;
  /**
   *    */
  private String startupDirName = USER_DIR + FILE_SEPARATOR + "startup";
  /**
   *    */
  private String pluginDirName = USER_DIR + FILE_SEPARATOR + "plugins";
  /**
   *    */
  private Integer messageCount = 0;
  /**
   *    */
  public static final boolean USE_PLUGINS = true;

  /**
   *
   */
  public final void init() {
    if (isPersisted()) {
      final File startupDir = new File(getStartupDirName());
      if (startupDir.mkdir()) {
        System.out.println("Failed to create startup directory: "
          + getStartupDirName());
      }
    }
    if (USE_PLUGINS) {
      final File pluginDir = new File(pluginDirName);
      if (pluginDir.mkdir()) {
        System.out.println("Failed to create plugin directory: "
          + pluginDirName);
      }
    }
  }

  /**
   * Load the configuration messages from disk and send them to the
   * configuration topic to recreate them when we startup.
   */
  public final void loadPlugins() {
    if (!(getStartupDirName() == null || startupDirName.isEmpty())) {
      if (!startupDirName.startsWith(FILE_SEPARATOR)) { // relative path
        setStartupDirName(USER_DIR + FILE_SEPARATOR + getStartupDirName());
      }

      final File startupDir = new File(getStartupDirName());
      if (startupDir.isDirectory()) {
        //FIXME: use FileFilter
        final File[] startupFiles = startupDir.listFiles();
        for (File startupFile : startupFiles) {
          BufferedReader reader = null;
          try {
            reader = new BufferedReader(new FileReader(startupFile));
            final StringBuilder config = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
              config.append(line);
              config.append("\n");
            }
            System.out.println("Sending config message");
            this.jmsTemplate.convertAndSend(getConfigTopic(),
              config.toString());
          } catch (FileNotFoundException e) {
            System.out.println(e);
          } catch (IOException e) {
            System.out.println(e);
          } finally {
            try {
              reader.close();
            } catch (IOException e) {
            }
          }
        }
      }
    }
  }

  /**
   * Create a {@link Plugin} implementation based on the <code>type</code> in
   * the XML message.
   *
   * @param doc The dom4j <code>Document</code> of the XML message.
   * @return An implementation of the <code>Plugin</code> interface.
   * @throws PipelineException
   */
  public final Plugin getPlugin(final Document doc) throws PipelineException {
    Plugin plugin = null;
    // pull out the individual String to make debugging easier!
    final String name = trim(doc.valueOf("//plugin/name"));
    final String type = trim(doc.valueOf("//plugin/type"));
    final String work = trim(doc.valueOf("//plugin/work"));
    final String inputName = trim(doc.valueOf("//plugin/destinations/input"));
    final String outputName = trim(doc.valueOf("//plugin/destinations/output"));
    final String replyToName
      = trim(doc.valueOf("//plugin/destinations/replyto"));
    final String xformJar = trim(doc.valueOf("//plugin/url"));
    try {

      final List<URL> xformURL = new ArrayList();
      // Enable loading of non-jar packaged plugins in the plugins directory:
      xformURL.add(new File(pluginDirName).toURI().toURL());
      // Enable re-loading of plugins in the distribution classpath
      // which is specified in the MANIFEST in the Pipeline.jar.
      if (xformJar == null || xformJar.isEmpty()) {
        // Look for Plugin implementations in the current classpath
        // and the plugins directory by default.
        final URL[] systemURLs
          = ((URLClassLoader) ClassLoader.getSystemClassLoader()).getURLs();
        xformURL.addAll(Arrays.asList(systemURLs));
      } else {
        // if specified look for an external .jar file
        xformURL.add(new URL(xformJar));
      }

      // Use a new instance of our "child first" ClassLoader so we can reload
      // plugins, and load new ones.
      final URL[] tclurls = {};
      final PluginClassLoader tcl
        = new PluginClassLoader(xformURL.toArray(tclurls));
      final Class transformClass = tcl.loadClass(type);

      final java.lang.reflect.Constructor constructor
        = transformClass.getConstructor(String.class, String.class, String.class,
          String.class, String.class, ConnectionFactory.class,
          JndiTemplate.class);
      plugin
        = (Plugin) constructor.newInstance(name, work, inputName, outputName,
          replyToName, connectionFactory, jndiTemplate);

    } catch (MalformedURLException ex) {
      throw new PipelineException("Bad jar URL", ex);
    } catch (InstantiationException ex) {
      throw new PipelineException("Instantiation failed", ex);
    } catch (IllegalAccessException ex) {
      throw new PipelineException("Illegal access", ex);
    } catch (IllegalArgumentException ex) {
      throw new PipelineException("Illegal argument", ex);
    } catch (InvocationTargetException ex) {
      throw new PipelineException("Invocation", ex);
    } catch (NoSuchMethodException ex) {
      throw new PipelineException("No such method", ex);
    } catch (SecurityException ex) {
      throw new PipelineException("Security Exception", ex);
    } catch (ClassNotFoundException ex) {
      throw new PipelineException("Class not found", ex);
    }
    if (plugin == null) {
      throw new PipelineException("Failed to create Plugin: " + name);
    }
    return plugin;
  }

  /**
   * Save the action message to a file on disk.
   *
   * @param name The name of the action.
   * @param xml The XML of the action.
   * @throws IOException If the file can not be written to disk.
   */
  public final void saveConfigMessage(final String name, final String xml)
    throws IOException {
    try (FileWriter writer = new FileWriter(getStartupDirName()
      + FILE_SEPARATOR + name + ".xml")) {
      writer.write(xml);
    }
  }

  /**
   *
   * @param name of configuration to remove
   */
  public final void removeConfigMessage(final String name) {
    final File config = new File(getStartupDirName()
      + FILE_SEPARATOR + name + ".xml");
    if (config.delete()) {
      System.out.println("Failed to delete config: " + name);
    }
  }

  /**
   * @return the map of plugins.
   */
  public final Map<String, Plugin> getPlugins() {
    return plugins;
  }

  /**
   *
   * @param inString the String to trim
   * @return the trimmed String
   */
  public final String trim(final String inString) {
    return (inString == null) ? "" : inString.trim();
  }

  /**
   * @return the jndiTemplate
   */
  public final JndiTemplate getJndiTemplate() {
    return jndiTemplate;
  }

  /**
   * @param inJndiTemplate the Spring JNDI Template
   */
  public final void setJndiTemplate(final JndiTemplate inJndiTemplate) {
    this.jndiTemplate = inJndiTemplate;
  }

  /**
   * @return the connectionFactory
   */
  public final ConnectionFactory getConnectionFactory() {
    return connectionFactory;
  }

  /**
   * @param inConnectionFactory the JMS ConnectionFactory
   */
  public final void setConnectionFactory(
    final ConnectionFactory inConnectionFactory) {
    this.connectionFactory = inConnectionFactory;
  }

  /**
   * @return the jmsTemplate
   */
  public final JmsTemplate getJmsTemplate() {
    return jmsTemplate;
  }

  /**
   * @param inJmsTemplate the Spring JmsTemplate
   */
  public final void setJmsTemplate(final JmsTemplate inJmsTemplate) {
    this.jmsTemplate = inJmsTemplate;
  }

  /**
   * @return the configTopic
   */
  public final Destination getConfigTopic() {
    return configTopic;
  }

  /**
   * @param inConfigTopic set the JMS Destination for configuration messages
   */
  public final void setConfigTopic(final Destination inConfigTopic) {
    this.configTopic = inConfigTopic;
  }

  /**
   * @return the validated
   */
  public final boolean isValidated() {
    return validated;
  }

  /**
   * @param inValidated set to true to validate XML DTD
   */
  public final void setValidated(final boolean inValidated) {
    this.validated = inValidated;
  }

  /**
   * @return the persisted
   */
  public final boolean isPersisted() {
    return persisted;
  }

  /**
   * @param inPersisted true if configuration is persisted
   */
  public final void setPersisted(final boolean inPersisted) {
    this.persisted = inPersisted;
  }

  /**
   * @return the startupDirName
   */
  public final String getStartupDirName() {
    return startupDirName;
  }

  /**
   * @param inStartupDirName name of directory with configuration files to load
   */
  public final void setStartupDirName(final String inStartupDirName) {
    this.startupDirName = inStartupDirName;
  }

  /**
   * @return the applicationContext
   */
  public final ClassPathXmlApplicationContext getApplicationContext() {
    return applicationContext;
  }

  /**
   * @param inApplicationContext the Spring Application Context
   */
  public final void setApplicationContext(
    final ClassPathXmlApplicationContext inApplicationContext) {
    this.applicationContext = inApplicationContext;
  }

  /**
   * @return the messageCount
   */
  @ManagedAttribute
  public final Integer getMessageCount() {
    return messageCount;
  }

  /**
   * @param inMessageCount the message count
   */
  public final void setMessageCount(final Integer inMessageCount) {
    this.messageCount = inMessageCount;
  }

  /**
   * @return the pluginDirName
   */
  public final String getPluginDirName() {
    return pluginDirName;
  }

  /**
   * @param inPluginDirName name of plugin directory
   */
  public final void setPluginDirName(final String inPluginDirName) {
    this.pluginDirName = inPluginDirName;
  }

}
