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

public class GoogleDrivePutParametersTest {

    GoogleDrivePutParameters parameters;

    @Before
    public void setUp() throws Exception {
        parameters = new GoogleDrivePutParameters("destination", "resource", true, "fileName");
    }

    @Test
    public void testGetDestinationFolderName() throws Exception {
        assertEquals("destination", parameters.getDestinationFolderId());
    }

    @Test
    public void testGetResourceName() throws Exception {
        assertEquals("resource", parameters.getResourceName());
    }

    @Test
    public void testIsOverwriteIfExist() throws Exception {
        assertTrue(parameters.isOverwriteIfExist());
    }

    @Test
    public void testGetFromLocalFilePath() throws Exception {
        assertEquals("fileName", parameters.getFromLocalFilePath());
    }

    @Test
    public void testGetFromBytes() throws Exception {
        GoogleDrivePutParameters p2 = new GoogleDrivePutParameters("destination", "resource", true, "byteContent".getBytes());
        assertEquals("byteContent", new String(p2.getFromBytes()));
    }

}
