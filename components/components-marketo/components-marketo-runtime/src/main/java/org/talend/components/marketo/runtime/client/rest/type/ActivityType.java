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
package org.talend.components.marketo.runtime.client.rest.type;

import java.util.Map;

public class ActivityType extends MarketoAttributes {

    private Integer id;

    private String name;

    private String description;

    private Map<String, String> primaryAttribute;

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return this.id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }

    public void setPrimaryAttribute(Map<String, String> primaryAttribute) {
        this.primaryAttribute = primaryAttribute;
    }

    public Map<String, String> getPrimaryAttribute() {
        return this.primaryAttribute;
    }
}
