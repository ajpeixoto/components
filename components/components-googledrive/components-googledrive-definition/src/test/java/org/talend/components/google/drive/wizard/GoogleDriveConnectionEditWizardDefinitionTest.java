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
package org.talend.components.google.drive.wizard;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.talend.components.google.drive.wizard.GoogleDriveConnectionEditWizardDefinition.COMPONENT_WIZARD_NAME;

import org.junit.Before;
import org.junit.Test;

public class GoogleDriveConnectionEditWizardDefinitionTest {

    GoogleDriveConnectionEditWizardDefinition definition;

    @Before
    public void setUp() throws Exception {
        definition = new GoogleDriveConnectionEditWizardDefinition();
    }

    @Test
    public void testGetName() throws Exception {
        assertThat(definition.getName(), is(COMPONENT_WIZARD_NAME));
    }

    @Test
    public void testIsTopLevel() throws Exception {
        assertFalse(definition.isTopLevel());
    }

}
