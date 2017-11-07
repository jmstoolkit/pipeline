/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jmstoolkit.logging;

import com.jmstoolkit.JTKException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

/**
 *
 * @author scott
 */
public class JTKConsoleFormatter extends SimpleFormatter {

  /** */
  private static final SimpleDateFormat RFC822 =
    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US);

  @Override
  public final String format(final LogRecord record) {
    final StringBuilder message = new StringBuilder();
    message.append("#######################################");
    message.append("#######################################\n");
    synchronized (this) {
      message.append(RFC822.format(record.getMillis()));
    }
    message.append(" ");
    message.append(record.getLoggerName());
    message.append(" ");
    message.append(record.getLevel().getName());
    message.append("\n");
    message.append(record.getSourceClassName());
    message.append(" ");
    message.append(record.getSourceMethodName());
    message.append(" ");
    message.append(record.getThreadID());
    message.append("\n");
    message.append(record.getMessage());
    message.append("\n");
    final Throwable exception = record.getThrown();
    if (exception != null) {
      message.append(JTKException.formatException(exception));
    }
    return message.toString();
  }

}
