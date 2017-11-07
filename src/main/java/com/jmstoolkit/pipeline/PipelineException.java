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

import com.jmstoolkit.JTKException;

/**
 *
 * @author Scott Douglass
 */
public class PipelineException extends JTKException {

  /**
   * Constructor.
   *
   * @param message the message text
   */
  public PipelineException(final String message) {
    super(message);
  }

  /** Constructor 2.
   *
   * @param inMessage the text message
   * @param inException the Exception
   */
  public PipelineException(final String inMessage,
    final Throwable inException) {
    super(inMessage, inException);
  }
}
