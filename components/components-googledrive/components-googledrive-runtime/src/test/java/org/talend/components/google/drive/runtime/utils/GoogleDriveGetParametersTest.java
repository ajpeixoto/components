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
package org.talend.components.google.drive.runtime.utils;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.google.drive.GoogleDriveMimeTypes;

public class GoogleDriveGetParametersTest {

    GoogleDriveGetParameters parameters;

    @Before
    public void setUp() throws Exception {
        parameters = new GoogleDriveGetParameters("resource", GoogleDriveMimeTypes.newDefaultMimeTypesSupported(), true,
                "outfile", true, true);
    }

    @Test
    public void testGetResourceName() throws Exception {
        assertEquals("resource", parameters.getResourceId());
    }

    @Test
    public void testGetMimeType() throws Exception {
        assertEquals(GoogleDriveMimeTypes.newDefaultMimeTypesSupported(), parameters.getMimeType());
    }

    @Test
    public void testIsStoreToLocal() throws Exception {
        assertTrue(parameters.isStoreToLocal());
    }

    @Test
    public void testGetOutputFileName() throws Exception {
        assertEquals("outfile", parameters.getOutputFileName());
    }

    @Test
    public void testIsAddExt() throws Exception {
        assertTrue(parameters.isAddExt());
    }

    @Test
    public void testSetOutputFileName() throws Exception {
        parameters.setOutputFileName("outputFile");
        assertEquals("outputFile", parameters.getOutputFileName());
    }
    
    @Test
    public void testIsCreateByteArray() throws Exception {
        assertTrue(parameters.isCreateByteArray());
    }

}
