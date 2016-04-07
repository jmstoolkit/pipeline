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
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import javax.jms.Destination;
import javax.naming.NamingException;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.jndi.JndiTemplate;

/**
 *
 * @author scott
 */
@ManagedResource(objectName = "jmstoolkit:name=jmsLoggingHandler",
  description = "A JMX-managed JMS Logging Handler")
public class JTKHandler extends Handler {

  /** */
  private Destination destination;
  /** */
  private JndiTemplate jndiTemplate;
  /** */
  private JmsTemplate jmsTemplate;
  /** */
  private String destinationName;

  /**
   *
   */
  @Override
  public void close() {
    // no op
  }

  /**
   *
   */
  @Override
  public void flush() {
    //no op
  }

  /**
   *
   * @param record the LogRecord to publish
   */
  @Override
  public final void publish(final LogRecord record) {
    this.getJmsTemplate().convertAndSend(this.getFormatter().format(record));
  }

  /** */
  public JTKHandler() {
    super();
  }

  /**
   *
   * @param inDestinationName JNDI name of the JMS Destination
   */
  public JTKHandler(final String inDestinationName) {
    super();
    destinationName = inDestinationName;
  }

  /**
   *
   * @param inDestination a JMS Destination
   */
  public JTKHandler(final Destination inDestination) {
    super();
    destination = inDestination;
  }


  /**
   *
   * @param inDestination a JMS Destination
   */
  public final synchronized void setDestination(
    final Destination inDestination) {
    this.destination = inDestination;
  }

  /**
   * @return the JMS Destination
   */
  public final synchronized Destination getDestination() {
    return destination;
  }

  /**
   *
   * @param inJndiTemplate a Spring Framework JndiTemplate
   */
  public final void setJndiTemplate(final JndiTemplate inJndiTemplate) {
    this.jndiTemplate = inJndiTemplate;
  }

  /**
   *
   * @return a Spring Framework JndiTemplate
   */
  public final JndiTemplate getJndiTemplate() {
    return jndiTemplate;
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
   * @return a Spring Framework JmsTemplate
   */
  public final JmsTemplate getJmsTemplate() {
    return jmsTemplate;
  }

  /**
   *
   * @param inDestinationName the JMS Destination
   */
  @ManagedAttribute
  public final void setDestinationName(final String inDestinationName) {
    this.destinationName = inDestinationName;
    try {
      this.setDestination((Destination)
        this.getJndiTemplate().lookup(this.destinationName));
    } catch (NamingException e) {
      System.err.println("Failed to set Destination: "
        + e.getLocalizedMessage());
    }
  }

  /**
   *
   * @return the name of the JMS Destination
   */
  @ManagedAttribute
  public final String getDestinationName() {
    return destinationName;
  }

  /**
   *
   * @param newLevel logging level
   */
  @ManagedAttribute
  public final void setLevelName(final String newLevel) {
    super.setLevel(Level.parse(newLevel.toUpperCase(Locale.getDefault())));
  }

  /**
   *
   * @return the logging level name
   */
  @ManagedAttribute
  public final String getLevelName() {
    return super.getLevel().getName();
  }
}
