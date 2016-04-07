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
import java.io.StringReader;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.naming.NamingException;
import org.apache.commons.dbcp.BasicDataSource;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jndi.JndiTemplate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An implementation of the {@link Transform} interface which replaces
 * values in XML with values from an SQL query. Uses JDBC for database
 * connections.
 *
 * @author Scott Douglass
 */
public class XMLValueTransform extends AbstractPlugin {

  /** Logger for this class. */
  private static final Logger LOGGER =
    Logger.getLogger(XMLValueTransform.class.getName());
  /** Use a pooling JDBC DataSource by default. */
  private BasicDataSource defaultDataSource = new BasicDataSource();
  //private DriverManagerDataSource defaultDataSource =
  //new DriverManagerDataSource();
  /** Spring JdbcTemplate. */
  private SimpleJdbcTemplate jdbcTemplate;
  /** List of transforms. */
  private final List<XMLValueTransformer> xforms =
    new LinkedList<XMLValueTransformer>();

  /**
   * Constructor for creating a useful <code>XMLValueTransform</code>.
   *
   * @param pluginName The human readable name for the Transform.
   * @param inConfig The SQL query for looking up the value.
   * @param inputName The JNDI name of the JMS Destination to subscribe to.
   * @param outputName The JNDI name of the JMS Destination to publish to.
   * @param replyToName The JNDI name of the JMS Destination for reply/status.
   * @param connectionFactory A JMS ConnectionFactory implementation.
   * @param inJndiTemplate The JndiTemplate.
   */
  public XMLValueTransform(final String pluginName, final String inConfig,
    final String inputName,
    final String outputName, final String replyToName,
    final ConnectionFactory connectionFactory,
    final JndiTemplate inJndiTemplate) {
    super();
    this.jndiTemplate = inJndiTemplate;
    this.name = pluginName;
    this.inName = inputName;
    this.outName = outputName;
    this.replyToName = replyToName;
    try {
      // Setup the JMS Destinations
      setInput((Destination) getJndiTemplate().lookup(
        inputName, Destination.class));
      setOutput((Destination) getJndiTemplate().lookup(
        outputName, Destination.class));
      setReplyTo((Destination) getJndiTemplate().lookup(
        replyToName, Destination.class));
      // Setup the JmsTemplate
      getJmsTemplate().setConnectionFactory(connectionFactory);
      getJmsTemplate().setDefaultDestination(getOutput());
      // Setup the default JDBC DataSource:
      final Document doc = getWork(inConfig);
      defaultDataSource.setDriverClassName(
        trim(doc.valueOf("//enrich/defaultDatabase/driver")));
      defaultDataSource.setUrl(
        trim(doc.valueOf("//enrich/defaultDatabase/url")));
      defaultDataSource.setUsername(
        trim(doc.valueOf("//enrich/defaultDatabase/username")));
      defaultDataSource.setPassword(
        trim(doc.valueOf("//enrich/defaultDatabase/password")));
      //FIXME: pool properties hard coded here
      defaultDataSource.setMaxActive(5);
      defaultDataSource.setMaxIdle(2);
      defaultDataSource.setMinIdle(1);

      jdbcTemplate = new SimpleJdbcTemplate(getDefaultDataSource());

      // create list of value transforms from the elements list
      final List<Element> elements =
        doc.selectNodes("//enrich/elements/element");
      for (Element node : elements) {
        SimpleJdbcTemplate vttemplate = jdbcTemplate;
        final String ddriver = node.valueOf("database/driver");
        if (ddriver != null && !"".equals(ddriver)) {
          // normally we'll use the defaultDataSource which is a pooling source
          // but we give the ability to specify a unique DataSource as well
          final DriverManagerDataSource dmds = new DriverManagerDataSource();
          dmds.setDriverClassName(trim(node.valueOf("database/driver")));
          dmds.setUrl(trim(node.valueOf("database/url")));
          dmds.setUsername(trim(node.valueOf("database/username")));
          dmds.setPassword(trim(node.valueOf("database/password")));
          vttemplate = new SimpleJdbcTemplate(dmds);
        }
        final XMLValueTransformer xvt = new XMLValueTransformer(vttemplate);
        xvt.setSrcPath(trim(node.valueOf("srcPath")));
        xvt.setDstPath(trim(node.valueOf("dstPath")));
        xvt.setSql(trim(node.valueOf("sql"))); // validates SQL statement
        xforms.add(xvt);
      }
    } catch (NamingException ex) {
      LOGGER.log(Level.SEVERE, "Bad JNDI name for Destination: "
        + getName(), ex);
      setStatus(STATUS_FAILED);
    } catch (DocumentException ex) {
      LOGGER.log(Level.SEVERE, "Invalid work XML for transform: ", ex);
      setStatus(STATUS_FAILED);
    } catch (XMLValueTransformException ex) {
      LOGGER.log(Level.SEVERE, "Bad work XML: ", ex);
      setStatus(STATUS_FAILED);
    } catch (SQLException ex) {
      LOGGER.log(Level.SEVERE, "Bad SQL query: ", ex);
    }
  }

  /**
   * Method to parse and validate the <code>work</code> XML String.
   *
   * @param work The XML to be validated.
   * @return The validated XML.
   * @throws DocumentException When the XML fails to validation.
   * @throws XMLValueTransformException When additional validation fails.
   */
  private Document getWork(final String work) throws DocumentException,
    XMLValueTransformException {
    // validate vs. DTD: enrich.dtd
    final SAXReader saxReader = new SAXReader(true);
    final Document doc = saxReader.read(new StringReader(work));
    // additional validation...
    try {
      Class.forName(trim(doc.valueOf("//enrich/defaultDatabase/driver")));
    } catch (ClassNotFoundException ex) {
      throw new XMLValueTransformException(
        "Default database driver not found in classpath: " + ex);
    }
    return doc;
  }

  /**
   * Implementation of JMS <code>MessageListener</code> interface. Performs
   * the work when a message is received.
   *
   * @param message The JMS Message received.
   */
  @Override
  public final void onMessage(final Message message) {
    String messageId = "";
    try {
      messageId = message.getJMSMessageID();
    } catch (JMSException ex) {
      LOGGER.log(Level.SEVERE, "Failed to get message id", ex);
    }
    if (message instanceof TextMessage) {
      try {
        final String body = ((TextMessage) message).getText();

        System.out.println("############################################");
        System.out.println("Transform input message received by "
          + getName() + ":");
        System.out.println(body.substring(0, 78));
        System.out.println("############################################");

        try {
          // do NOT validate vs. DTD
          final SAXReader saxReader = new SAXReader();
          Document doc = saxReader.read(new StringReader(body));
          int count = 0;
          for (XMLValueTransformer xvt : this.getXforms()) {
            try {
              doc = xvt.transform(doc);
              count++;
            } catch (DataAccessException ex) {
              LOGGER.log(Level.WARNING, messageId + " JDBC failed: "
                + xvt.toString(), ex);
            }
          }
          LOGGER.log(
            Level.INFO, "{0} {1} of {2} XMLValueTransformers succeeded",
            new Object[]{messageId, count, getXforms().size()});
          LOGGER.log(Level.INFO, "{0} Transform performed by service: {1}",
            new Object[]{messageId, getName()});

          getJmsTemplate().convertAndSend(doc.asXML());

          this.operationCount++;
        } catch (DocumentException ex) {
          LOGGER.log(Level.SEVERE, messageId + " Unable to parse XML message: "
            + body + ". ", ex);
        }
      } catch (JMSException ex) {
        LOGGER.log(Level.SEVERE, messageId
          + " Failed to get message text: ", ex);
      }
    } else {
      LOGGER.log(Level.WARNING,
        "{0} Message is not a TextMessage. TextMessages only please.",
        new Object[]{messageId});
    }
  }

  /**
   * @return the defaultDataSource
   */
  public final BasicDataSource getDefaultDataSource() {
    return defaultDataSource;
  }

  /**
   * @param inDefaultDataSource the JDBC data source
   */
  public final void setDefaultDataSource(BasicDataSource inDefaultDataSource) {
    this.defaultDataSource = inDefaultDataSource;
  }

  /**
   * @return the jdbcTemplate
   */
  public final SimpleJdbcTemplate getJdbcTemplate() {
    return jdbcTemplate;
  }

  /**
   * @param inJdbcTemplate the Spring JdbcTemplate
   */
  public final void setJdbcTemplate(final SimpleJdbcTemplate inJdbcTemplate) {
    this.jdbcTemplate = inJdbcTemplate;
  }

  /**
   * @return the xforms
   */
  public final List<XMLValueTransformer> getXforms() {
    return xforms;
  }
}
