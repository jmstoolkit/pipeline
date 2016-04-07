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

import java.net.URLClassLoader;
import java.net.URL;
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
public class PluginClassLoaderTest {

  private static URL testPluginJarURL;
  private static URL testPluginDirURL;
  private static final String FAKE_PLUGIN = "com.jmstoolkit.pipeline.plugin.Fake";
  private static final String SYSTEM_PLUGIN = "com.jmstoolkit.pipeline.plugin.Echo";

  public PluginClassLoaderTest() {
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
    testPluginDirURL = new URL("file:///" + System.getProperty("user.dir") + "/plugin-test/");
    testPluginJarURL = new URL("file:///" + System.getProperty("user.dir") + "/test.jar");
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
   * Test of loading class using system class loader URLs.
   */
  @Test
  public void testLoadClassFromSystemClasspath() throws Exception {
    System.out.println("loadClass from system class path");
    PluginClassLoader instance =
      new PluginClassLoader(
      ((URLClassLoader) ClassLoader.getSystemClassLoader()).getURLs());
    Class result = instance.loadClass(SYSTEM_PLUGIN, true);
    assertEquals(SYSTEM_PLUGIN, result.getName());
  }

  /**
   * Test of loading a class from the plugin path, instead of
   * the system class path.
   * @throws Exception
   */
  @Test
  public void testLoadClassFromPluginDir() throws Exception {
    System.out.println("loadClass from plugin directory outside of system class path");
    PluginClassLoader instance =
      new PluginClassLoader(new URL[] { testPluginDirURL });
    Class result = instance.loadClass(FAKE_PLUGIN, true);
    assertEquals(FAKE_PLUGIN, result.getName());
  }

  /**
   * Test of loading a plugin from a jar file not on the system class path.
   * @throws Exception
   */
  @Test
  public void testLoadClassFromPluginJar() throws Exception {
    System.out.println("loadClass from plugin jar outside of system class path");
    PluginClassLoader instance =
      new PluginClassLoader(new URL[] { testPluginJarURL });
    Class result = instance.loadClass(FAKE_PLUGIN, true);
    assertEquals(FAKE_PLUGIN, result.getName());
  }
}
