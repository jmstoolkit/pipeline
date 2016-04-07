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

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 *
 * @author Scott Douglass
 */
public class JTKLogRecord extends LogRecord {

  /** Host from which the message originates. */
  private String hostname;

  /**
   *
   * @param level the logging level
   * @param message the message text
   */
  public JTKLogRecord(final Level level, final String message) {
    super(level, message);
  }

  /**
   * @return the hostname
   */
  public final String getHostname() {
    return hostname;
  }

  /**
   * @param inHostname the host originating the message
   */
  public final void setHostname(final String inHostname) {
    this.hostname = inHostname;
  }

  /**
   * @return the date
   */
  public final Date getDate() {
    return new Date(this.getMillis());
  }
}
