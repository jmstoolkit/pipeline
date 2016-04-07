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

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.File;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Scott Douglass
 */
public class XSLTransformerTest {

  private static final String INPUT_FILE = System.getProperty("user.dir") + "/test-xsl/test-input.xml";
  private static final String RESULT_FILE = System.getProperty("user.dir") + "/test-xsl/test-output.xml";
  private static final String XSLT_FILE = System.getProperty("user.dir") + "/test-xsl/test-xslt.xsl";
  private static String INXML;
  private static String OUTXML;
  private static String XSLT;

  public XSLTransformerTest() {
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
    INXML = loadTextFile(INPUT_FILE);
    OUTXML = loadTextFile(RESULT_FILE);
    XSLT = loadTextFile(XSLT_FILE);
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  /**
   * Test of transform method, of class XSLTransformer.
   */
  @Test
  public void testTransform() throws Exception {
    System.out.println("transform: xml to xml using xslt");
    XSLTransformer instance = new XSLTransformer();
    instance.setXslt(XSLT);
    System.out.println("Input xml:");
    System.out.println(INXML);
    System.out.println("XSLT:");
    System.out.println(XSLT);
    System.out.println("Expected result:");
    System.out.println(OUTXML);
    String result = instance.transform(INXML);
    System.out.println("Actual result:");
    System.out.println(result);
    assertEquals(OUTXML, result);
  }

  public static String loadTextFile(String inFileName) throws Exception {
    BufferedInputStream messageFileStream = null;
    File messageFile = new File(inFileName);
    StringBuilder messageString = new StringBuilder("");
    byte[] buffer = new byte[4096];
    messageFileStream =
      new BufferedInputStream(new FileInputStream(messageFile));
    while (messageFileStream.read(buffer) != -1) {
      messageString.append(new String(buffer));
    }
    messageFileStream.close();
    return messageString.toString().trim();
  }
}
