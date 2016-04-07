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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.LogRecord;
import java.util.logging.XMLFormatter;

/**
 * Inserts a hostname entity into the generated XML so that
 * we can easily identify which host generated the log message.
 *
 * @author Scott Douglass
 */
public class JTKXMLFormatter extends XMLFormatter {

  /**
   *
   * @param record a LogRecord
   * @return the log record as a String
   */
  @Override
  public final String format(final LogRecord record) {
    final StringBuilder logrecord = new StringBuilder(super.format(record));
    String hostname = "unknown";
    try {
      hostname = InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException ex) {
      System.out.println("Couldn't get hostname: " + ex.getMessage());
    }
    logrecord.insert(10 /* <record>\n */, "  <hostname>"
      + hostname + "</hostname>\n");
    return logrecord.toString();
  }
}
