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

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.talend.components.api.component.runtime.ReaderDataProvider;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.common.dataset.runtime.DatasetRuntime;
import org.talend.components.google.drive.data.GoogleDriveDatasetProperties;
import org.talend.components.google.drive.data.GoogleDriveInputProperties;
import org.talend.daikon.java8.Consumer;
import org.talend.daikon.properties.ValidationResult;

public class GoogleDriveDatasetRuntime implements DatasetRuntime<GoogleDriveDatasetProperties> {

    private GoogleDriveDatasetProperties dataset;

    private RuntimeContainer container;

    @Override
    public ValidationResult initialize(RuntimeContainer container, GoogleDriveDatasetProperties properties) {
        this.container = container;
        this.dataset = properties;

        return ValidationResult.OK;
    }

    @Override
    public Schema getSchema() {
        return dataset.getSchema();
    }

    @Override
    public void getSample(int limit, Consumer<IndexedRecord> consumer) {
        GoogleDriveInputProperties properties = new GoogleDriveInputProperties("sample");
        properties.setDatasetProperties(dataset);
        GoogleDriveInputReader reader = (GoogleDriveInputReader) createDataSource(properties).createReader(container);
        reader.setLimit(limit);
        ReaderDataProvider<IndexedRecord> provider = new ReaderDataProvider<>(reader, limit, consumer);
        provider.retrieveData();
    }

    public GoogleDriveDataSource createDataSource(GoogleDriveInputProperties properties) {
        GoogleDriveDataSource ds = new GoogleDriveDataSource();
        ds.initialize(container, properties);
        ds.validate(container);
        return ds;
    }
}
