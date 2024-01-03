//============================================================================
//
// Copyright (C) 2006-2024 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
//============================================================================
package org.talend.components.common.avro;

import java.sql.DatabaseMetaData;

/**
 * a model which provide all the information to fetch the meta data from a database table
 *
 */
public class JDBCTableMetadata {

    /**
     * database cataglog
     */
    private String catalog;

    /**
     * database schema
     */
    private String dbSchema;

    /**
     * table name
     */
    private String tablename;

    /**
     * JDBC DatabaseMetaData object, the entrance to access the JDBC API
     */
    private DatabaseMetaData DatabaseMetaData;

    public String getCatalog() {
        return catalog;
    }

    public JDBCTableMetadata setCatalog(String catalog) {
        this.catalog = catalog;
        return this;
    }

    public String getDbSchema() {
        return dbSchema;
    }

    public JDBCTableMetadata setDbSchema(String dbSchema) {
        this.dbSchema = dbSchema;
        return this;
    }

    public String getTablename() {
        return tablename;
    }

    public JDBCTableMetadata setTablename(String tablename) {
        this.tablename = tablename;
        return this;
    }

    public DatabaseMetaData getDatabaseMetaData() {
        return DatabaseMetaData;
    }

    public JDBCTableMetadata setDatabaseMetaData(DatabaseMetaData databaseMetaData) {
        DatabaseMetaData = databaseMetaData;
        return this;
    }

}
