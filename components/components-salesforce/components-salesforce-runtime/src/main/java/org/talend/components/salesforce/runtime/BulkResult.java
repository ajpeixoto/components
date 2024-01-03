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
package org.talend.components.salesforce.runtime;

import java.util.Map;
import java.util.TreeMap;

public class BulkResult {

    Map<String, Object> values;

    public BulkResult() {
        values = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    }

    public void setValue(String field, Object vlaue) {
        values.put(field, vlaue);
    }

    public Object getValue(String fieldName) {
        return values.get(fieldName);
    }

    public void copyValues(BulkResult result) {
        if (result == null) {
            return;
        } else {
            for (String key : result.values.keySet()) {
                Object value = result.values.get(key);
                if ("#N/A".equals(value)) {
                    value = null;
                }
                values.put(key, value);
            }
        }
    }

    public boolean containField(String fieldName) {
        if (values != null && values.containsKey(fieldName)) {
            return true;
        }
        return false;
    }
}