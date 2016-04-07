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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.naming.NamingException;
import org.springframework.jndi.JndiTemplate;

/**
 *
 * @author Scott Douglass
 */
public class Echo extends AbstractPlugin {
  private static final Logger LOGGER = Logger.getLogger(Echo.class.getName());

  /**
   *
   * @param pluginName name of the plugin
   * @param inConfig configuration for plugin
   * @param inputName JMS input Destination
   * @param outputName JMS output Destination
   * @param errorsName JMS errors Destination
   * @param connectionFactory JMS ConnectionFactory
   * @param inJndiTemplate Spring JndiTemplate
   */
  public Echo(final String pluginName, final String inConfig,
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
    } catch (NamingException ex) {
      LOGGER.log(Level.SEVERE, "Bad JNDI name for Destination: "
        + getName(), ex);
      setStatus(STATUS_FAILED);
    }
  }

  @Override
  public final void onMessage(final Message inMessage) {
    try {
      if (inMessage instanceof TextMessage) {
        getJmsTemplate().convertAndSend(
          (Object) ((TextMessage) inMessage).getText(),
          new BasicMessageProcessor(inMessage.getJMSCorrelationID()));
      } else {
        getJmsTemplate().convertAndSend(
          (Object) "Non-text message received by Echo",
          new BasicMessageProcessor(inMessage.getJMSCorrelationID()));
      }
      this.operationCount++;
    } catch (JMSException ex) {
      LOGGER.log(Level.SEVERE, "Echo had JMS failure onMessage!", ex);
    }
  }

}
