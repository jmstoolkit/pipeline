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
package com.jmstoolkit.pipeline.plugin;

import com.jmstoolkit.pipeline.AbstractPlugin;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.naming.NamingException;
import org.springframework.jndi.JndiTemplate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An implementation of the {@link Transform} interface which uses an
 * {@link XSLTransformer} to transform an input XML message and publish
 * the transformed XML message.
 *
 * @author Scott Douglass
 */
public class XMLTransform extends AbstractPlugin {

  /** The Logger for this class. */
  private static final Logger LOGGER =
    Logger.getLogger(XMLTransform.class.getName());
  /** XSLTransormer, cool! */
  private final XSLTransformer xslt = new XSLTransformer();

  /**
   * Constructor for creating a useful <code>XMLTransform</code>.
   *
   * @param pluginName The human readable name for the Transform.
   * @param inXsl The XSL to be used.
   * @param inputName The JNDI name of the JMS Destination to subscribe to.
   * @param outputName The JNDI name of the JMS Destination to publish to.
   * @param errorsName The JNDI name of the JMS Destination to send errors.
   * @param connectionFactory A JMS ConnectionFactory implementation.
   * @param inJndiTemplate A Spring Framework JndiTemplate.
   */
  public XMLTransform(final String pluginName, final String inXsl,
    final String inputName,
    final String outputName, final String errorsName,
    final ConnectionFactory connectionFactory,
    final JndiTemplate inJndiTemplate) {
    super();
    this.jndiTemplate = inJndiTemplate;
    setName(pluginName);
    setInName(inputName);
    setOutName(outputName);
    setReplyToName(errorsName);
    try {
      setInput((Destination) getJndiTemplate().lookup(
        inputName, Destination.class));
      setOutput((Destination) getJndiTemplate().lookup(
        outputName, Destination.class));
      setReplyTo((Destination) getJndiTemplate().lookup(
        errorsName, Destination.class));
      //
      getJmsTemplate().setConnectionFactory(connectionFactory);
      getJmsTemplate().setDefaultDestination(getOutput());
      //
      xslt.setXslt(inXsl);
    } catch (NamingException ex) {
      LOGGER.log(Level.SEVERE, "Bad JNDI name for Destination: "
        + getName(), ex);
      setStatus(STATUS_FAILED);
    } catch (XSLTransformerException e) {
      LOGGER.log(Level.SEVERE, "Bad XSLT", e);
      setStatus(STATUS_FAILED);
    }
  }

  /**
   * Implementation of JMS <code>MessageListener</code> interface. Performs
   * the work when a message is received.
   *
   * @param message The JMS Message received.
   */
  @Override
  public final void onMessage(final Message message) {
    String messageId = "xxx";
    try {
      messageId = message.getJMSMessageID();
    } catch (JMSException ex) {
      LOGGER.log(Level.SEVERE, "Failed to get message id", ex);
    }
    try {
      if (message instanceof TextMessage) {
        final String body = trim(((TextMessage) message).getText());
        try {
          System.out.println("############################################");
          System.out.println("Transform input message received by "
            + getName() + ":");
          System.out.println(body.subSequence(0, 78));
          System.out.println("############################################");
          LOGGER.log(Level.INFO, "{0} Transform performed by service: {1}",
            new Object[]{messageId, getName()});
          getJmsTemplate().convertAndSend(getXslt().transform(body));
          this.setOperationCount(this.getOperationCount() + 1);
        } catch (XSLTransformerException ex) {
          LOGGER.log(Level.SEVERE, messageId + " XSL Transform failed: ", ex);
        }
      }
    } catch (JMSException ex) {
      LOGGER.log(Level.SEVERE, messageId + " Failed to get message text: ", ex);
    }
  }

  /**
   * @return the xslt transformer
   */
  public final XSLTransformer getXslt() {
    return xslt;
  }
}
