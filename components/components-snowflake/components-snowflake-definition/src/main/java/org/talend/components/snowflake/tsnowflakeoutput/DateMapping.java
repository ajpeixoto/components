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
package org.talend.components.snowflake.tsnowflakeoutput;

/**
 * Snowflake supported Date and Time data types
 * This enum reflects possible values for DI Date (java.util.Date) type mapping
 */
public enum DateMapping {
    DATE,
    DATETIME,
    TIME,
    TIMESTAMP,
    TIMESTAMP_LTZ,
    TIMESTAMP_NTZ,
    TIMESTAMP_TZ
}
