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

import java.util.Map;

import org.talend.components.google.drive.GoogleDriveMimeTypes.MimeType;

public class GoogleDriveGetParameters {

    private final String resourceId;

    private final Map<String, MimeType> mimeTypeForExport;

    private final boolean storeToLocal;

    private String outputFileName;

    private final boolean addExt;

    private final boolean createByteArray;

    public GoogleDriveGetParameters(String resourceId, Map<String, MimeType> mimeType, boolean storeToLocal,
            String outputFileName, boolean addExt, boolean createByteArray) {
        this.resourceId = resourceId;
        this.mimeTypeForExport = mimeType;
        this.storeToLocal = storeToLocal;
        this.outputFileName = outputFileName;
        this.addExt = addExt;
        this.createByteArray = createByteArray;
    }

    public String getResourceId() {
        return resourceId;
    }

    public Map<String, MimeType> getMimeType() {
        return mimeTypeForExport;
    }

    public boolean isStoreToLocal() {
        return storeToLocal;
    }

    public String getOutputFileName() {
        return outputFileName;
    }

    public boolean isAddExt() {
        return addExt;
    }

    public boolean isCreateByteArray() {
        return createByteArray;
    }

    public void setOutputFileName(String outputFileName) {
        this.outputFileName = outputFileName;
    }
}
