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
package org.talend.components.jdbc.runtime.type;

public class DebugUtil {

    private String[] splits;

    private StringBuffer strBuffer;

    public DebugUtil(String sql) {
        sql += " ";
        splits = sql.split("\\?");
        strBuffer = new StringBuffer(32);
    }

    private int index = 0;

    public void writeHead() {
        if (index < splits.length) {
            strBuffer.append(splits[index++]);
        }
    }

    public void writeColumn(String columnContent, boolean textEnclose) {
        if (index < splits.length) {
            if (textEnclose) {
                strBuffer.append("'");
            }
            strBuffer.append(columnContent);
            if (textEnclose) {
                strBuffer.append("'");
            }
            strBuffer.append(splits[index++]);
        }
    }

    public String getSQL() {
        String sql = strBuffer.toString();
        index = 0;
        strBuffer = new StringBuffer(32);
        return sql;
    }

}
