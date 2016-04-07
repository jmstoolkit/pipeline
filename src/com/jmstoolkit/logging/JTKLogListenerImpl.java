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
package com.jmstoolkit.logging;

import java.util.Locale;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.jndi.JndiTemplate;

/**
 *
 * @author scott
 */
public class JTKLogListenerImpl implements MessageListener {

  /** */
  private JmsTemplate jmsTemplate;
  /** */
  private JndiTemplate jndiTemplate;

  /**
   *
   * @param arg0 the JMS Message
   */
  @Override
  public final void onMessage(final Message arg0) {
    try {
      if (arg0 instanceof TextMessage) {
        System.out.printf(Locale.getDefault(),
          "Logger service got the message:\n %s \n",
          ((TextMessage) arg0).getText());
      } else {
        System.out.printf(Locale.getDefault(),
          "Logger service got the message. JMS ID: %s\n",
          arg0.getJMSMessageID());
      }
    } catch (JMSException e) {
      System.err.printf(Locale.getDefault(), "JMS Failure: \n %s\n", e);
    }
  }

  /**
   *
   * @param inJmsTemplate a Spring Framework JmsTemplate
   */
  public final void setJmsTemplate(final JmsTemplate inJmsTemplate) {
    this.jmsTemplate = inJmsTemplate;
  }

  /**
   *
   * @return Spring Framework JmsTemplate
   */
  public final JmsTemplate getJmsTemplate() {
    return jmsTemplate;
  }

  /**
   *
   * @param inJndiTemplate Spring Framework JndiTemplate
   */
  public final void setJndiTemplate(final JndiTemplate inJndiTemplate) {
    this.jndiTemplate = inJndiTemplate;
  }

  /**
   *
   * @return Spring Framework JndiTemplate
   */
  public final JndiTemplate getJndiTemplate() {
    return jndiTemplate;
  }
}
