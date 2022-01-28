/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.persistence.jdbc.db;

import org.knowm.yank.Yank;
import org.openhab.core.items.Item;
import org.openhab.core.types.State;
import org.openhab.persistence.jdbc.dto.ItemVO;
import org.openhab.persistence.jdbc.dto.ItemsVO;
import org.openhab.persistence.jdbc.utils.StringUtilsExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extended Database Configuration class. Class represents
 * the extended database-specific configuration. Overrides and supplements the
 * default settings from JdbcBaseDAO. Enter only the differences to JdbcBaseDAO here.
 *
 * @author Helmut Lehmeyer - Initial contribution
 */
public class JdbcSqliteDAO extends JdbcBaseDAO {
    private final Logger logger = LoggerFactory.getLogger(JdbcSqliteDAO.class);

    /********
     * INIT *
     ********/
    public JdbcSqliteDAO() {
        super();
        initSqlQueries();
        initSqlTypes();
        initDbProps();
    }

    private void initSqlQueries() {
        logger.debug("JDBC::initSqlQueries: '{}'", this.getClass().getSimpleName());
        sqlGetDB = "PRAGMA DATABASE_LIST"; // "SELECT SQLITE_VERSION()"; // "PRAGMA DATABASE_LIST"->db Path/Name
                                           // "PRAGMA SCHEMA_VERSION";
        sqlIfTableExists = "SELECT name FROM sqlite_master WHERE type='table' AND name='#searchTable#'";
        sqlCreateItemsTableIfNot = "CREATE TABLE IF NOT EXISTS #itemsManageTable# (ItemId INTEGER PRIMARY KEY AUTOINCREMENT, #colname# #coltype# NOT NULL)";
        sqlInsertItemValue = "INSERT OR IGNORE INTO #tableName# (TIME, VALUE) VALUES( #tablePrimaryValue#, CAST( ? as #dbType#) )";
    }

    /**
     * INFO: http://www.java2s.com/Code/Java/Database-SQL-JDBC/StandardSQLDataTypeswithTheirJavaEquivalents.htm
     */
    private void initSqlTypes() {
        logger.debug("JDBC::initSqlTypes: Initialize the type array");
        sqlTypes.put("tablePrimaryValue", "strftime('%Y-%m-%d %H:%M:%f' , 'now' , 'localtime')");
    }

    /**
     * INFO: https://github.com/brettwooldridge/HikariCP
     */
    private void initDbProps() {
        // Properties for HikariCP
        databaseProps.setProperty("driverClassName", "org.sqlite.JDBC");
        // driverClassName OR BETTER USE dataSourceClassName
        // databaseProps.setProperty("dataSourceClassName", "org.sqlite.SQLiteDataSource");
    }

    /**************
     * ITEMS DAOs *
     **************/

    @Override
    public String doGetDB() {
        return Yank.queryColumn(sqlGetDB, "file", String.class, null).get(0);
    }

    @Override
    public ItemsVO doCreateItemsTableIfNot(ItemsVO vo) {
        String sql = StringUtilsExt.replaceArrayMerge(sqlCreateItemsTableIfNot,
                new String[] { "#itemsManageTable#", "#colname#", "#coltype#" },
                new String[] { vo.getItemsManageTable(), vo.getColname(), vo.getColtype() });
        logger.debug("JDBC::doCreateItemsTableIfNot sql={}", sql);
        Yank.execute(sql, null);
        return vo;
    }

    /*************
     * ITEM DAOs *
     *************/
    @Override
    public void doStoreItemValue(Item item, State itemState, ItemVO vo) {
        ItemVO storedVO = storeItemValueProvider(item, itemState, vo);
        String sql = StringUtilsExt.replaceArrayMerge(sqlInsertItemValue,
                new String[] { "#tableName#", "#dbType#", "#tablePrimaryValue#" },
                new String[] { storedVO.getTableName(), storedVO.getDbType(), sqlTypes.get("tablePrimaryValue") });
        Object[] params = new Object[] { storedVO.getValue() };
        logger.debug("JDBC::doStoreItemValue sql={} value='{}'", sql, storedVO.getValue());
        Yank.execute(sql, params);
    }

    /****************************
     * SQL generation Providers *
     ****************************/

    /*****************
     * H E L P E R S *
     *****************/

    /******************************
     * public Getters and Setters *
     ******************************/
}
