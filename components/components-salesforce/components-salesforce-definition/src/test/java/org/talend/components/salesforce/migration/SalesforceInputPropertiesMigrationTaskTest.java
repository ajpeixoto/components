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
package org.talend.components.salesforce.migration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;
import org.talend.components.salesforce.TestUtils;
import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputProperties;
import org.talend.components.salesforce.tsalesforceoutput.TSalesforceOutputProperties;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.serialize.SerializerDeserializer.Deserialized;

public class SalesforceInputPropertiesMigrationTaskTest {

    @Test
    public void testSafetySwitch() throws IOException {
        //Loading old SalesforceInput properties and check if value was initialized correctly.
        Deserialized<TSalesforceInputProperties> deser = Properties.Helper.fromSerializedPersistent(
                TestUtils.getResourceAsString(getClass(), "tSalesforceInputProperties_701.json"),
                TSalesforceInputProperties.class);

        TSalesforceInputProperties properties = deser.object;
        assertTrue(properties.safetySwitch.getValue());
    }

    @Test
    public void testEndpointChange() throws IOException {
        Deserialized<TSalesforceInputProperties> deser = Properties.Helper.fromSerializedPersistent(
                TestUtils.getResourceAsString(getClass(), "tSalesforceInputProperties_701.json"),
                TSalesforceInputProperties.class);

        TSalesforceInputProperties properties = deser.object;
        assertEquals("\"https://login.salesforce.com/services/Soap/u/42.0\"",
                properties.connection.endpoint.getValue());
    }
    @Test
    public void testQueryModePossibleValues() throws IOException {
        Deserialized<TSalesforceInputProperties> deser = Properties.Helper.fromSerializedPersistent(
                TestUtils.getResourceAsString(getClass(), "tSalesforceInputProperties_721.json"),
                TSalesforceInputProperties.class);

        TSalesforceInputProperties properties = deser.object;
        assertEquals(3,properties.queryMode.getPossibleValues().size());
        assertTrue(properties.queryMode.getPossibleValues().contains(TSalesforceInputProperties.QueryMode.BulkV2));
    }

    @Test
    public void testDataTimeValues() throws IOException {
        Deserialized<TSalesforceInputProperties> deser = Properties.Helper.fromSerializedPersistent(
                TestUtils.getResourceAsString(getClass(), "tSalesforceInputProperties_721.json"),
                TSalesforceInputProperties.class);

        TSalesforceInputProperties properties = deser.object;
        assertFalse(properties.dataTimeUTC.getValue());
    }

    @Test
    public void testDataTimeUTCValues() throws IOException {
        Deserialized<TSalesforceOutputProperties> deser = Properties.Helper.fromSerializedPersistent(
                TestUtils.getResourceAsString(getClass(), "tSalesforceOutputProperties_731.json"),
                TSalesforceOutputProperties.class);

        TSalesforceOutputProperties properties = deser.object;
        assertFalse(properties.dataTimeUTC.getValue());
    }

    @Test
    public void testIncludeDelete() throws IOException {
        Deserialized<TSalesforceInputProperties> deser = Properties.Helper.fromSerializedPersistent(
                TestUtils.getResourceAsString(getClass(), "tSalesforceInputProperties_621_inlude_delete.json"),
                TSalesforceInputProperties.class);

        TSalesforceInputProperties properties = deser.object;
        assertFalse(properties.includeDeleted.getValue());
    }
}
