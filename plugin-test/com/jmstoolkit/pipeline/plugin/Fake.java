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
public class Fake extends AbstractPlugin {
  private static final Logger LOGGER = Logger.getLogger(Fake.class.getName());

  public Fake(String pluginName, String inConfig, String inputName,
    String outputName, String errorsName, ConnectionFactory connectionFactory,
    JndiTemplate jndiTemplate) {
    this.jndiTemplate = jndiTemplate;
    setName(pluginName);
    setInName(inputName);
    setOutName(outputName);
    setReplyToName(errorsName);
  }

  @Override
  public void onMessage(Message inMessage) {
  }

}
