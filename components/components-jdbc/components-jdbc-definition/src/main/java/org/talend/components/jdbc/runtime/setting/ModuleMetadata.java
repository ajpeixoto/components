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
package org.talend.components.jdbc.runtime.setting;

import org.apache.avro.Schema;

// TODO a little duplicated with JDBCTableMetadata or make it common not only for JDBC
public class ModuleMetadata {

    public final String catalog;

    public final String dbschema;

    public final String name;

    public final String type;

    public final String comment;

    public final Schema schema;

    public ModuleMetadata(String catalog, String dbschema, String name, String type, String comment, Schema schema) {
        this.catalog = catalog;
        this.dbschema = dbschema;
        this.name = name;
        this.type = type;
        this.comment = comment;
        this.schema = schema;
    }
}
