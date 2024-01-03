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
package org.talend.components.salesforce.test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.talend.components.api.properties.ComponentReferenceProperties;
import org.talend.components.salesforce.TestUtils;
import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputProperties;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.serialize.SerializerDeserializer;

public class ConnectionPropertiesTest {

    @Test
    public void testDeserializeOldReferenceProps() throws IOException {
        String oldPropsStr = TestUtils.getResourceAsString(getClass(),"tSalesforceInputConnectionProperties_old.json");
        SerializerDeserializer.Deserialized<TSalesforceInputProperties> fromSerializedPersistent = Properties.Helper
                .fromSerializedPersistent(oldPropsStr, TSalesforceInputProperties.class);
        TSalesforceInputProperties deserializedProps = fromSerializedPersistent.object;
        ComponentReferenceProperties deSerRefProps = (ComponentReferenceProperties) deserializedProps
                .getProperty("connection.referencedComponent");
        assertEquals("tSalesforceConnection", deSerRefProps.referenceDefinitionName.getValue());
        assertEquals("tSalesforceConnection_1", deSerRefProps.componentInstanceId.getValue());
        assertEquals(ComponentReferenceProperties.ReferenceType.COMPONENT_INSTANCE, deSerRefProps.referenceType.getValue());
    }

}
