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
package org.talend.components.salesforce.runtime.dataprep;

import org.talend.components.salesforce.datastore.SalesforceDatastoreProperties;

public class CommonTestUtils {

    public static void setValueForDatastoreProperties(SalesforceDatastoreProperties datastore) {
        datastore.userId.setValue(System.getProperty("salesforce.user"));
        datastore.password.setValue(System.getProperty("salesforce.password"));
        datastore.securityKey.setValue(System.getProperty("salesforce.key"));
    }
    
}
