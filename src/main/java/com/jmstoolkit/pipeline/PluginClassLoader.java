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

import java.net.URL;
import java.net.URLClassLoader;

/**
 *
 * @author Scott Douglass
 */
public class PluginClassLoader extends URLClassLoader {

  /** The plugin package name. */
  private static final String PLUGIN_PACKAGE = "com.jmstoolkit.pipeline.plugin";

  /**
   *
   * @param urls of classes
   */
  public PluginClassLoader(final URL[] urls) {
    super(urls);
  }

  /**
   *
   * @param urls of classes
   * @param parent class loader
   */
  public PluginClassLoader(final URL[] urls, final ClassLoader parent) {
    super(urls, parent);
  }

  /**
   * We override the parent-first behavior established by
   * java.lang.Classloader.
   *
   * The implementation is surprisingly straightforward.
   * @param name of class
   * @param resolve try to resolve if true
   * @return the class
   * @throws ClassNotFoundException if it can't find the class
   */
  @Override
  protected final Class loadClass(final String name, final boolean resolve)
    throws ClassNotFoundException {

    // First, check if the class has already been loaded by this class loader
    Class pluginClass = findLoadedClass(name);
    final String pkg = name.substring(0, name.lastIndexOf('.'));

    // if not loaded, search the local (child) resources
    if (pluginClass == null && PLUGIN_PACKAGE.equals(pkg)) {
      try {
        pluginClass = findClass(name);
      } catch (ClassNotFoundException cnfe) {
        // ignore
      }
    }

    // if we could not find it, delegate to parent
    // Note that we don't attempt to catch any ClassNotFoundException
    if (pluginClass == null) {
      if (getParent() == null) {
         pluginClass = getSystemClassLoader().loadClass(name);
      } else {
        pluginClass = getParent().loadClass(name);
      }
    }

    if (resolve) {
      resolveClass(pluginClass);
    }

    return pluginClass;
  }

  /**
   * Override the parent-first resource loading model established by
   * java.lang.Classloader with child-first behavior.
   * @param name of the class
   * @return URL of the class
   */
  @Override
  public final URL getResource(final String name) {
    URL url = findResource(name);

    // if local search failed, delegate to parent
    if (url == null) {
      url = getParent().getResource(name);
    }
    return url;
  }
}
