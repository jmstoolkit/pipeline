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

import javax.jms.MessageListener;

/**
 * Interface defining the functionality of any <code>Plugin</code> which uses
 * JMS via the {@link javax.jms.MessageListener}.
 *
 * @author Scott Douglass
 */
public interface Plugin extends MessageListener {

  /**
   * Configure and start a <code>Plugin</code> <code>MessageConsumer</code>.
   */
  void init();

  /**
   * Stop a <code>Plugin</code> <code>MessageConsumer</code>.
   */
  void stop();

  /**
   * Get some information about the <code>Plugin</code>.
   *
   * @return the plugin information
   */
  String getInfo();

  /**
   * Gets the count of operations performed by the <code>Plugin</code>.
   *
   * @return the count of operations
   */
  Integer getOperationCount();
}
