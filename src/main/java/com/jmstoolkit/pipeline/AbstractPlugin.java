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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;
import org.springframework.jndi.JndiTemplate;

/**
 * An abstract implementation of the {@link Plugin} interface which can be
 * used as the base class for specific implementations.
 *
 * @author Scott Douglass
 */
public abstract class AbstractPlugin implements Plugin {

  private static final Logger LOGGER = Logger.getLogger(AbstractPlugin.class.getName());
  /**
   *
   */
  public static final String P_APP_NAME = "app.name";
  /**
   *
   */
  public static final String D_APP_NAME = "JMSToolKit";
  /**
   *
   */
  public static final String D_JNDI_PROPERTIES = "jndi.properties";
  /**
   *
   */
  public static final String P_CORRELATION_ID = "correlation_id";
  /**
   *
   */
  public static final String D_CORRELATION_ID = UUID.randomUUID().toString();
  /**
   *
   */
  public static final String CORRELATION_ID =
    System.getProperty(P_CORRELATION_ID, D_CORRELATION_ID);
  /**
   *
   */
  public static final String P_HOSTNAME = "hostname";
  /**
   *
   */
  public static final String D_HOSTNAME = "unknown";
  //private static final String P_ENCODING = "jmstoolkit.encoding";
  //private static final String D_ENCODING = "UTF-8";
  private static final String APP_NAME = "Pipeline Plugin";
  /**
   *
   */
  protected static final String STATUS_OK = "OK";
  /**
   *
   */
  protected static final String STATUS_FAILED = "FAIL";
  /**
   *
   */
  protected String name;
  /**
   *
   */
  protected String inName;
  /**
   *
   */
  protected String outName;
  /**
   *
   */
  protected String replyToName;
  /**
   *
   */
  protected Destination input;
  /**
   *
   */
  protected Destination replyTo;
  /**
   *
   */
  protected Destination output;
  /**
   *
   */
  protected MessageConsumer consumer;
  /**
   *
   */
  protected Connection connection;
  /**
   *
   */
  protected Session session;
  /**
   *
   */
  protected JmsTemplate jmsTemplate = new JmsTemplate();
  /**
   *
   */
  protected JndiTemplate jndiTemplate = new JndiTemplate();
  /**
   *
   */
  protected String status = STATUS_OK;
  /**
   *
   */
  protected Integer operationCount = 0;

  /**
   * Configures and starts the JMS MessageConsumer.
   */
  @Override
  public void init() {
    try {
      setConnection(getJmsTemplate().getConnectionFactory().createConnection());
      setSession(getConnection().createSession(false, Session.AUTO_ACKNOWLEDGE));
      setConsumer(getSession().createConsumer(getInput()));
      getConsumer().setMessageListener(this);
      getConnection().start();

      System.setProperty(P_APP_NAME, APP_NAME);
      try {
        System.setProperty(P_HOSTNAME,
          InetAddress.getLocalHost().getHostName());
      } catch (UnknownHostException e) {
        LOGGER.log(Level.WARNING, "Couldn't determine hostname", e);
        System.setProperty(P_HOSTNAME, D_HOSTNAME);
      }
    } catch (JMSException ex) {
      LOGGER.log(Level.SEVERE, "Failed to start MessageConsumer", ex);
      setStatus(STATUS_FAILED);
    }
  }

  /**
   * Stops the JMS MessageConsumer.
   */
  @Override
  public void stop() {
    try {
      getConsumer().setMessageListener(null);
      getConsumer().close();
      getConnection().stop();
    } catch (JMSException ex) {
      LOGGER.log(Level.SEVERE, "Failed to stop connection for " + getName(), ex);
    }
  }

  /**
   * Gets some basic information about the <code>Plugin</code>.
   *
   * @return Information about the transform.
   */
  @Override
  public String getInfo() {
    StringBuilder sb = new StringBuilder();
    sb.append("name=");
    sb.append(getName());
    sb.append(",type=");
    sb.append(this.getClass().getName());
    sb.append(",input=");
    sb.append(getInName());
    sb.append(",output=");
    sb.append(getOutName());
    sb.append(",replyto=");
    sb.append(getReplyToName());
    sb.append(",status=");
    sb.append(getStatus());
    sb.append(",operations=");
    sb.append(this.getOperationCount());
    return sb.toString();
  }

  /**
   *
   * @param inString
   * @return
   */
  public String trim(String inString) {
    String result = "";
    if (inString != null) {
      result = inString.trim();
    }
    return result;
  }

  /**
   *
   */
  public class BasicMessageProcessor implements MessagePostProcessor {
    private String correlationId;
    /**
     *
     * @param inCorrelationId
     */
    public BasicMessageProcessor(String inCorrelationId) {
      correlationId = inCorrelationId;
    }
    /**
     *
     * @param msg
     * @return
     * @throws JMSException
     */
    @Override
    public Message postProcessMessage(Message msg) throws JMSException {
      msg.setStringProperty("app", System.getProperty(P_APP_NAME, D_APP_NAME));
      msg.setStringProperty("user", System.getProperty("user.name"));
      msg.setStringProperty("host", System.getProperty("hostname"));
      if (correlationId == null || correlationId.isEmpty()) {
        msg.setJMSCorrelationID(
          System.getProperty(P_CORRELATION_ID, CORRELATION_ID));
      } else {
        msg.setJMSCorrelationID(correlationId);
      }
      return msg;
    }
  }

  /**
   *
   * @param arg0
   */
  @Override
  public abstract void onMessage(Message arg0);

  /**
   * @return the name
   */
  public final String getName() {
    return name;
  }

  /**
   * @param name the name to set
   */
  public final void setName(String name) {
    this.name = name;
  }

  /**
   * @return the inName
   */
  public final String getInName() {
    return inName;
  }

  /**
   * @param inName the inName to set
   */
  public final void setInName(String inName) {
    this.inName = inName;
  }

  /**
   * @return the outName
   */
  public final String getOutName() {
    return outName;
  }

  /**
   * @param outName the outName to set
   */
  public final void setOutName(String outName) {
    this.outName = outName;
  }

  /**
   * @return the errName
   */
  public final String getReplyToName() {
    return replyToName;
  }

  /**
   * @param replyToName
   */
  public final void setReplyToName(String replyToName) {
    this.replyToName = replyToName;
  }

  /**
   * @return the input
   */
  public final Destination getInput() {
    return input;
  }

  /**
   * @param input the input to set
   */
  public final void setInput(Destination input) {
    this.input = input;
  }

  /**
   * @return the replyto
   */
  public final Destination getReplyTo() {
    return replyTo;
  }

  /**
   * @param replyTo
   */
  public final void setReplyTo(Destination replyTo) {
    this.replyTo = replyTo;
  }

  /**
   * @return the output
   */
  public final Destination getOutput() {
    return output;
  }

  /**
   * @param output the output to set
   */
  public final void setOutput(Destination output) {
    this.output = output;
  }

  /**
   * @return the consumer
   */
  public MessageConsumer getConsumer() {
    return consumer;
  }

  /**
   * @param consumer the consumer to set
   */
  public void setConsumer(MessageConsumer consumer) {
    this.consumer = consumer;
  }

  /**
   * @return the connection
   */
  public Connection getConnection() {
    return connection;
  }

  /**
   * @param connection the connection to set
   */
  public void setConnection(Connection connection) {
    this.connection = connection;
  }

  /**
   * @return the session
   */
  public Session getSession() {
    return session;
  }

  /**
   * @param session the session to set
   */
  public void setSession(Session session) {
    this.session = session;
  }

  /**
   * @return the jmsTemplate
   */
  public JmsTemplate getJmsTemplate() {
    return jmsTemplate;
  }

  /**
   * @param jmsTemplate the jmsTemplate to set
   */
  public void setJmsTemplate(JmsTemplate jmsTemplate) {
    this.jmsTemplate = jmsTemplate;
  }

  /**
   * @return the jndiTemplate
   */
  public JndiTemplate getJndiTemplate() {
    return jndiTemplate;
  }

  /**
   * @param jndiTemplate the jndiTemplate to set
   */
  public void setJndiTemplate(JndiTemplate jndiTemplate) {
    this.jndiTemplate = jndiTemplate;
  }

  /**
   * @return the status
   */
  public String getStatus() {
    return status;
  }

  /**
   * @param status the status to set
   */
  public void setStatus(String status) {
    this.status = status;
  }

  /**
   * @return the count
   */
  @Override
  public Integer getOperationCount() {
    return operationCount;
  }

  /**
   * @param count the count to set
   */
  public void setOperationCount(Integer count) {
    this.operationCount = count;
  }
}
