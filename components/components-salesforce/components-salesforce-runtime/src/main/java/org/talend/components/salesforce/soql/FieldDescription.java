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
package org.talend.components.salesforce.soql;

import java.util.List;

/**
 * Describes SOQL query field, contains 
 * simple field name - name how it appears in Salesforce entity
 * full name - name which should be used in component avro schema
 * entity names - ordered list of Salesforce entities names to query to get field type   
 */
public class FieldDescription {

	private String fullName;

	private String simpleName;

	private List<String> entityNames;

	public FieldDescription(String fullName, String simpleName, List<String> entityNames) {
		super();
		this.fullName = fullName;
		this.simpleName = simpleName;
		this.entityNames = entityNames;
	}

	public String getFullName() {
		return fullName;
	}

	public String getSimpleName() {
		return simpleName;
	}

	public List<String> getEntityNames() {
		return entityNames;
	}

}
