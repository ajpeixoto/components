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

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class GoogleDriveGetResultTest {

    GoogleDriveGetResult result;

    @Before
    public void setUp() throws Exception {
        result = new GoogleDriveGetResult("uid", "content".getBytes());
    }

    @Test
    public void testGetId() throws Exception {
        assertEquals("uid", result.getId());
    }

    @Test
    public void testGetContent() throws Exception {
        assertEquals("content", new String(result.getContent()));
    }

}
