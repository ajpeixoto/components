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
package org.talend.components.jdbc.wizard;

/**
 * 
 * JDBC wizard for edit a existed JDBC meta data
 * also need to another wizard definition for retrieve the tables by right click.
 * Now this part can't work as need some work on TUP part too.
 */
public class JDBCConnectionEditWizardDefinition extends JDBCConnectionWizardDefinition {

    public static final String COMPONENT_WIZARD_NAME = "JDBC.edit";

    @Override
    public String getName() {
        return COMPONENT_WIZARD_NAME;
    }

    @Override
    public boolean isTopLevel() {
        return false;
    }

}
