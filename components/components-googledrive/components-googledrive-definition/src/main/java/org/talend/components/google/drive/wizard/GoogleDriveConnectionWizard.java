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

import org.talend.components.api.wizard.ComponentWizard;
import org.talend.components.api.wizard.ComponentWizardDefinition;
import org.talend.components.google.drive.connection.GoogleDriveConnectionProperties;

public class GoogleDriveConnectionWizard extends ComponentWizard {

    GoogleDriveConnectionProperties connection;

    /**
     * This shall be called by the service creation and is not supposed to be called by any client.
     *
     * @param definition wizard definition
     * @param repositoryLocation store location
     */
    public GoogleDriveConnectionWizard(ComponentWizardDefinition definition, String repositoryLocation) {
        super(definition, repositoryLocation);
        connection = new GoogleDriveConnectionProperties("connection");
        connection.init();
        connection.setRepositoryLocation(getRepositoryLocation());
        addForm(connection.getForm(GoogleDriveConnectionProperties.FORM_WIZARD));
    }

    public void setupProperties(GoogleDriveConnectionProperties connectionProperties) {
        connection.setupProperties();
        connection.copyValuesFrom(connectionProperties);
    }

}
