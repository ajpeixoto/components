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

import java.util.Arrays;

import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.common.datastore.runtime.DatastoreRuntime;
import org.talend.components.google.drive.data.GoogleDriveDatasetProperties;
import org.talend.components.google.drive.data.GoogleDriveDatastoreProperties;
import org.talend.components.google.drive.data.GoogleDriveInputProperties;
import org.talend.daikon.properties.ValidationResult;

public class GoogleDriveDatastoreRuntime implements DatastoreRuntime<GoogleDriveDatastoreProperties> {

    protected GoogleDriveDatastoreProperties datastore;

    @Override
    public Iterable<ValidationResult> doHealthChecks(RuntimeContainer container) {
        GoogleDriveDataSource ds = new GoogleDriveDataSource();
        GoogleDriveInputProperties properties = new GoogleDriveInputProperties("health");
        GoogleDriveDatasetProperties dataset = new GoogleDriveDatasetProperties("data");
        dataset.setDatastoreProperties(datastore);
        properties.setDatasetProperties(dataset);
        ds.initialize(container, properties);

        return Arrays.asList(ds.validate(container));
    }

    @Override
    public ValidationResult initialize(RuntimeContainer container, GoogleDriveDatastoreProperties properties) {
        datastore = properties;
        return ValidationResult.OK;
    }
}
