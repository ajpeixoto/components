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
package org.talend.components.google.drive.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.google.drive.connection.GoogleDriveConnectionProperties;

public class GoogleDriveSourceTest extends GoogleDriveTestBaseRuntime {

    private GoogleDriveConnectionProperties properties;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        properties = new GoogleDriveConnectionProperties("test");
        properties.setupProperties();
        properties = (GoogleDriveConnectionProperties) setupConnectionWithInstalledApplicationWithIdAndSecret(properties);
        properties.setupLayout();

        source.initialize(container, properties);
    }

    @Test
    public void testSplitIntoBundles() throws Exception {
        assertEquals(1, source.splitIntoBundles(10, container).size());
    }

    @Test
    public void testGetEstimatedSizeBytes() throws Exception {
        assertEquals(0, source.getEstimatedSizeBytes(container));
    }

    @Test
    public void testProducesSortedKeys() throws Exception {
        assertFalse(source.producesSortedKeys(container));
    }

    @Test
    public void testCreateReader() throws Exception {
        assertNull(source.createReader(container));
    }

}
