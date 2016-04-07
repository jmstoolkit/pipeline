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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 *
 * @author Scott Douglass
 */
public class XSLTransformer {

  /** XSL Transformer Factory. */
  private static final TransformerFactory TRANSFORMER_FACTORY =
    TransformerFactory.newInstance();
  /** XML Tranformer. */
  private Transformer transformer;
  /** The XSL. */
  private String xslt;

  /**
   *
   * @param inXslt the XSL text
   * @throws XSLTransformerException if the transformer can't be created
   */
  public final void setXslt(final String inXslt)
    throws XSLTransformerException {
    if (inXslt == null || inXslt.trim().isEmpty()) {
      throw new XSLTransformerException("XSL string was null or empty.");
    }
    this.xslt = inXslt.trim();
    final StringReader xslReader = new StringReader(this.xslt);
    final Source xslSource = new StreamSource(xslReader);
    try {
      setTransformer(TRANSFORMER_FACTORY.newTransformer(xslSource));
    } catch (TransformerConfigurationException ex) {
      throw new XSLTransformerException("Failed to create xslTransformer", ex);
    }
  }

  /**
   *
   * @return the XSL
   */
  public final String getXslt() {
    return this.xslt;
  }

  /**
   *
   * @param inXslFile XSL text file
   * @throws XSLTransformerException if the file is invalid
   */
  public final void setXslt(final File inXslFile)
    throws XSLTransformerException {

    final StringBuilder xsl = new StringBuilder();
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new FileReader(inXslFile));
      String line;
      while ((line = reader.readLine()) != null) {
        xsl.append(line.trim());
        xsl.append("\n");
      }
      this.setXslt(xsl.toString().trim());
    } catch (IOException ex) {
      throw new XSLTransformerException("Error reading XSLT file", ex);
    } finally {
      try {
        reader.close();
      } catch (IOException ex) {
        // ignore
      }
    }
  }

  /**
   *
   * @param inXml the XML text
   * @param inXsl the XSL text
   * @return the transformed XML
   * @throws XSLTransformerException on error
   */
  public final String transform(final String inXml, final String inXsl)
    throws XSLTransformerException {
    this.setXslt(inXsl);
    return transform(inXml);
  }

  /**
   *
   * @param inXml the XML text
   * @return the transformed XML
   * @throws XSLTransformerException on error
   */
  public final String transform(final String inXml)
    throws XSLTransformerException {
    if (inXml == null || inXml.trim().isEmpty()) {
      throw new XSLTransformerException("Input string was null or empty.");
    }
    if (this.xslt == null || this.xslt.isEmpty()) {
      throw new XSLTransformerException("XSL string was null or empty.");
    }

    final Source xmlSource = new StreamSource(new StringReader(inXml.trim()));
    final StringWriter outXml = new StringWriter();
    final Result xmlResult = new StreamResult(outXml);

    try {
      this.getTransformer().transform(xmlSource, xmlResult);
    } catch (TransformerConfigurationException e) {
      throw new XSLTransformerException("Failed to create XSL transformer", e);
    } catch (TransformerException e) {
      throw new XSLTransformerException("XSL transformer barfed", e);
    }

    return outXml.toString().trim();
  }

  /**
   * @return the transformer
   */
  public final Transformer getTransformer() {
    return transformer;
  }

  /**
   * @param inTransformer the transformer to set
   */
  public final void setTransformer(final Transformer inTransformer) {
    this.transformer = inTransformer;
  }
}
