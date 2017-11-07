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

import java.sql.SQLException;
import java.util.Locale;
import javax.sql.DataSource;
import org.dom4j.Document;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Update an XML document with data from a database.
 *
 * @author Scott Douglass
 */
public class XMLValueTransformer {

  /** Spring JdbcTemplate. */
  private JdbcTemplate jdbcTemplate;
  /** SQL query. */
  private String sql;
  /** Xpath for source in XML. */
  private String srcPath;
  /** Xpath for destination in XML. */
  private String dstPath;
  /** JDBC data source. */
  private DataSource dataSource;
  /** Is it safe? */
  private Boolean safe = true;

  /**
   *
   * @param inJdbcTemplate the Spring JdbcTemplate
   */
  public XMLValueTransformer(final JdbcTemplate inJdbcTemplate) {
    this.jdbcTemplate = inJdbcTemplate;
  }

  /**
   *
   * @param inDataSource the JDBC data source
   */
  public XMLValueTransformer(final DataSource inDataSource) {
    this.jdbcTemplate = new JdbcTemplate(inDataSource);
    this.dataSource = inDataSource;
  }

  /**
   * Get one value from a database and convert it to a <code>String</code>.
   * @param where A single value to be passed to a parameterized SQL query
   * @return A <code>String<code> representation of the single result
   * @throws DataAccessException if there's a JDBC failure
   */
  public final String getValue(final String where) throws DataAccessException {
    return this.getJdbcTemplate().queryForObject(sql, String.class, where);
  }

  /**
   * Update one XML node with data from a database.
   * @param doc The dom4j <code>Document</code>
   * @return the XML Document
   * @throws DataAccessException if there's a JDBC exception
   */
  public final Document transform(final Document doc)
    throws DataAccessException {
    if (dstPath == null || dstPath.isEmpty()) {
      dstPath = srcPath;
    }
    final String result = this.getValue(doc.valueOf(srcPath));
    doc.selectSingleNode(dstPath).setText(result);
    return doc;
  }

  /**
   *
   * @return the configuration as a String
   */
  @Override
  public final String toString() {
    final StringBuilder signature = new StringBuilder();
    signature.append("srcPath=");
    signature.append(srcPath);
    signature.append(",dstPath=");
    signature.append(dstPath);
    signature.append(",sql=");
    signature.append(sql);
    return signature.toString();
  }

  /**
   * @return the jdbcTemplate
   */
  public final JdbcTemplate getJdbcTemplate() {
    return jdbcTemplate;
  }

  /**
   * @param inJdbcTemplate the Spring JdbcTemplate
   */
  public final void setJdbcTemplate(final JdbcTemplate inJdbcTemplate) {
    this.jdbcTemplate = inJdbcTemplate;
  }

  /**
   * @return the sql
   */
  public final String getSql() {
    return sql;
  }

  /**
   * @param inSql the sql to set
   * @throws SQLException if there's an error
   */
  public final void setSql(final String inSql) throws SQLException {
    if (safe) {
      if (inSql != null && !"".equals(inSql)
        && inSql.toLowerCase(Locale.getDefault()).startsWith("select ")) {
        this.sql = inSql;
      } else {
        throw new SQLException("SQL statement must beging with 'select'.");
      }
    } else {
      this.sql = inSql;
    }
  }

  /**
   * @return the srcPath
   */
  public final String getSrcPath() {
    return srcPath;
  }

  /**
   * @param inSrcPath source xpath
   */
  public final void setSrcPath(final String inSrcPath) {
    this.srcPath = inSrcPath;
  }

  /**
   * @return the dstPath
   */
  public final String getDstPath() {
    return dstPath;
  }

  /**
   * @param inDstPath destination xpath
   */
  public final void setDstPath(final String inDstPath) {
    this.dstPath = inDstPath;
  }

  /**
   * @return the dataSource
   */
  public final DataSource getDataSource() {
    return dataSource;
  }

  /**
   * @param inDataSource the JDBC data source
   */
  public final void setDataSource(final DataSource inDataSource) {
    this.dataSource = inDataSource;
  }

  /**
   * @return the safe
   */
  public final Boolean isSafe() {
    return safe;
  }

  /**
   * @param inSafe is the plugin in safe mode
   */
  public final void setSafe(final Boolean inSafe) {
    this.safe = inSafe;
  }
}
