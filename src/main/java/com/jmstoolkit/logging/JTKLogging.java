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

import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author scott
 */
public class JTKLogging {

  /** */
  private Handler logHandler;
  /** */
  private Properties loggerProperties;
  /** */
  private Boolean useParentHandlers = true;
  /** */
  private Formatter formatter;
  /** */
  private Handler consoleHandler;
  /** */
  public static final String CONSOLE_LOGGER_NAME = "JTKCONSOLE";
  /** */
  private static final Logger CONSOLE = Logger.getLogger(CONSOLE_LOGGER_NAME);


  /**
   *
   */
  public JTKLogging() {
  }

  /**
   *
   * @param inProperties
   */
  public JTKLogging(final Properties inProperties) {
    loggerProperties = inProperties;
    this.createLoggers();
  }

  /**
   *
   */
  private void createLoggers() {
    for (Object loggerName : this.getLoggerProperties().keySet()) {
      final String levelName =
        this.getLoggerProperties().getProperty((String) loggerName);
      try {
        final Level level = Level.parse(levelName);
        final Logger logger = Logger.getLogger((String) loggerName);
        logger.setLevel(level);
        if (logger.getName().equals(CONSOLE_LOGGER_NAME)) {
          logger.addHandler(this.getConsoleHandler());
        } else {
          logger.addHandler(this.getLogHandler());
        }
        logger.setUseParentHandlers(this.getUseParentHandlers());
      } catch (IllegalArgumentException e) {
        CONSOLE.log(Level.SEVERE,
          "Failed to set Level {0} for Logger {1}",
          new Object[] {levelName, loggerName});
      }
    }
  }

  /**
   *
   * @param inLogHandler the Handler
   */
  public final void setLogHandler(final Handler inLogHandler) {
    this.logHandler = inLogHandler;
  }

  /**
   *
   * @return the Handler
   */
  public final Handler getLogHandler() {
    return logHandler;
  }

  /**
   *
   * @param inLoggerProperties logging properties
   */
  public final void setLoggerProperties(final Properties inLoggerProperties) {
    this.loggerProperties = inLoggerProperties;
  }

  /**
   *
   * @return the logging Properties
   */
  public final Properties getLoggerProperties() {
    return loggerProperties;
  }

  /**
   *
   * @param inUseParentHandlers true or false
   */
  public final void setUseParentHandlers(final Boolean inUseParentHandlers) {
    this.useParentHandlers = inUseParentHandlers;
  }

  /**
   *
   * @return true if using parent handlers, false otherwise
   */
  public final Boolean getUseParentHandlers() {
    return useParentHandlers;
  }

  /**
   *
   * @param inFormatter the Formatter
   */
  public final void setFormatter(final Formatter inFormatter) {
    this.formatter = inFormatter;
  }

  /**
   *
   * @return the Formatter
   */
  public final Formatter getFormatter() {
    return formatter;
  }

  /**
   * @return the consoleFormatter
   */
  public final Handler getConsoleHandler() {
    return consoleHandler;
  }

  /**
   * @param inConsoleHandler the consoleFormatter
   */
  public final void setConsoleHandler(final Handler inConsoleHandler) {
    this.consoleHandler = inConsoleHandler;
  }
}
