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

/**
 *
 * @author Scott Douglass
 */
public class XSLTransformerException extends Exception {

  /**
   *
   * @param inMessage String for message
   */
  public XSLTransformerException(final String inMessage) {
    super(inMessage);
  }

  /**
   * 
   * @param inMessage The message
   * @param inException The original Exception
   */
  public XSLTransformerException(final String inMessage,
    final Throwable inException) {
    super(inMessage, inException);
  }
}
