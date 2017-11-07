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
import com.jmstoolkit.Settings;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

/**
 *
 * @author Scott Douglass
 */
public class Main {

  /**
   *
   * @param args
   */
  public static void main(final String[] args) {
    try {
      Settings.loadSystemSettings("jndi.properties");
      Settings.loadSystemSettings("app.properties");
    } catch (JTKException ex) {
      System.out.println("Failed to load application settings");
      System.out.println(JTKException.formatException(ex));
      System.exit(1);
    }

    final ClassPathXmlApplicationContext applicationContext =
      new ClassPathXmlApplicationContext(
      new String[]{"/infrastructure-context.xml",
        "/mdb-context.xml",
        "/jmx-context.xml",
        "/logging-context.xml"});
    applicationContext.start();

    final DefaultMessageListenerContainer dmlc =
      (DefaultMessageListenerContainer) applicationContext.getBean(
      "listenerContainer");
    if (dmlc != null) {
      dmlc.start();
      final Pipeline pipeline =
        (Pipeline) applicationContext.getBean("pipelineService");
      // enable access to the original application context
      pipeline.setApplicationContext(applicationContext);

      // Insure that the Pipeline loads configuration files AFTER
      // the listenerContainer is running.
      while (!dmlc.isRunning()) {
        try {
          Thread.sleep(100);
        } catch (InterruptedException ex) {
        }
      }
      pipeline.loadPlugins();
      pipeline.sendProperties();
      while (true) {
        try {
          Thread.sleep(1000);
        } catch (InterruptedException ex) {
        }
      }
    }
  }
}
