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

import java.io.File;
import org.apache.derby.jdbc.EmbeddedDataSource40;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import java.sql.Connection;
import java.sql.PreparedStatement;
import org.apache.commons.io.FileUtils;
import org.dom4j.Document;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author scott
 */
public class XMLValueTransformerTest {

  /** The XMLValueTransformer to use in the tests. */
  private static XMLValueTransformer XFORM;
  /** SQL to perform lookup. */
  private static final String SQL_SELECT =
    "select name from currency where alias = ?";
  /** SQL to create the lookup table. */
  private static final String SQL_CREATE_TABLE =
    "create table currency (id INT, name VARCHAR(3), alias VARCHAR(3))";
  /** SQL to insert a row into the lookup table. */
  private static final String SQL_INSERT_ROW = 
    "insert into currency values (1,'USD','XXX')";
  /** The source XML document. */
  private static Document SOURCE;
  /** The expected result XML document, after the lookup.*/
  private static Document RESULT;
  /** The Derby database name. */
  private static final String DB_NAME="XDB";
  
  public XMLValueTransformerTest() {
  }

  /**
   * Starts an embedded Derby database, creates the lookup table and
   * inserts one row for the test. Also creates the source and expected
   * result XML documents. Finally, initializes the XMLValueTransformer.
   * 
   * @throws Exception on JDBC problems
   */
  @BeforeClass
  public static void setUpClass() throws Exception {
    // loads the "driver" which apparently means the database is running
    Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
    PreparedStatement ps;
    //Connection connection = DriverManager.getConnection("jdbc:derby:XDB;create=true");     
    
    EmbeddedDataSource40 dataSource = new EmbeddedDataSource40();
    dataSource.setCreateDatabase("create");
    dataSource.setDatabaseName(DB_NAME);
    Connection connection = dataSource.getConnection();
    ps = connection.prepareStatement(SQL_CREATE_TABLE);
    ps.execute();
    ps = connection.prepareStatement(SQL_INSERT_ROW);
    ps.execute();
    ps.close();
    // Create the transformer
    XFORM = new XMLValueTransformer(dataSource);
    XFORM.setSql(SQL_SELECT);
    XFORM.setSrcPath("/trade/currency");

    // Create the XML SOURCE document
    SOURCE = DocumentHelper.createDocument();
    Element root = SOURCE.addElement("trade");
    root.addElement("currency").addText("XXX");
    // create the expected result document for comparison
    RESULT = DocumentHelper.createDocument();
    root = RESULT.addElement("trade");
    root.addElement("currency").addText("USD");
  }

  /**
   * Removes the Derby DB used for the tests.
   * @throws Exception if there is file system problem
   */
  @AfterClass
  public static void tearDownClass() throws Exception {
    File dbDir = new File(System.getProperty("user.dir") +
      System.getProperty("file.separator") + DB_NAME);
    if (dbDir.exists()) {
      FileUtils.deleteDirectory(dbDir);
    }
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  /**
   * Test of getValue method, of class XMLValueTransformer. Makes sure that the
   * lookup SQL is working. We should look for XXX and get USD.
   */
  @Test
  public void testGetValue() {
    System.out.println("getValue");
    String where = "XXX";
    String expResult = "USD";
    String result = XFORM.getValue(where);
    assertEquals(expResult, result);
  }

  /**
   * Test of transform method, of class XMLValueTransformer. Makes sure the
   * XPath is correct, and then the lookup is correct.
   */
  @Test
  public void testTransform() {
    System.out.println("transform");
    Document result = XFORM.transform(SOURCE);
    assertEquals(RESULT.asXML(), result.asXML());
  }
}
