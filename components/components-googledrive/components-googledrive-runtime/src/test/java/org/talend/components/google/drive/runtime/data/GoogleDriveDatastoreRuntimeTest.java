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
package org.talend.components.google.drive.runtime.data;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;

public class GoogleDriveDatastoreRuntimeTest extends GoogleDriveDataBaseTest {

    GoogleDriveDatastoreRuntime rt;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        rt = new GoogleDriveDatastoreRuntime();
    }

    @Test
    public void testDoHealthChecks() throws Exception {
        rt.initialize(container, datastore);
        Iterable<ValidationResult> r = rt.doHealthChecks(container);
        assertNotNull(r);
    }

    @Test
    public void testInitialize() throws Exception {
        assertEquals(Result.OK, rt.initialize(container, datastore).getStatus());
    }
}
