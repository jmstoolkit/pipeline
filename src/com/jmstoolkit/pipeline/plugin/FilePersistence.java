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

import com.jmstoolkit.pipeline.AbstractPlugin;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.Message;
import javax.naming.NamingException;
import org.springframework.jndi.JndiTemplate;

/**
 * This plugin should be able to take a message and persist it to a database
 * using Hibernate. When the plugin is configured, the configuration specifies
 * the class which should have been annotated.
 *
 * @author Scott Douglass
 */
public class FilePersistence extends AbstractPlugin {

  /** Logger for this class. */
  private static final Logger LOGGER =
    Logger.getLogger(FilePersistence.class.getName());

  /**
   *
   * @param pluginName the name of the plugin
   * @param inConfig the plugin configuration
   * @param inputName JMS input Destination
   * @param outputName JMS output Destination
   * @param errorsName JMS error Destination
   * @param connectionFactory JMS ConnectionFactory
   * @param inJndiTemplate Spring JndiTemplate
   */
  public FilePersistence(final String pluginName, final String inConfig,
    final String inputName,
    final String outputName, final String errorsName,
    final ConnectionFactory connectionFactory,
    final JndiTemplate inJndiTemplate) {
    super();
    this.jndiTemplate = inJndiTemplate;
    setName(pluginName);
    setInName(inputName);
    //setOutName(outputName);
    //setReplyToName(errorsName);
    try {
      setInput((Destination) getJndiTemplate().lookup(
        inputName, Destination.class));
      //setOutput((Destination) getJndiTemplate().lookup(
      //outputName, Destination.class));
      //setReplyTo((Destination) getJndiTemplate().lookup(
      //errorsName, Destination.class));
      //
      //getJmsTemplate().setConnectionFactory(connectionFactory);
      //getJmsTemplate().setDefaultDestination(getOutput());
      //
    } catch (NamingException ex) {
      LOGGER.log(Level.SEVERE, "Bad JNDI name for Destination: "
        + getName(), ex);
      setStatus(STATUS_FAILED);
    }
  }

  /**
   * @param arg0 message received
   */
  @Override
  public final void onMessage(final Message arg0) {
    throw new UnsupportedOperationException("Not supported yet.");
  }
}
