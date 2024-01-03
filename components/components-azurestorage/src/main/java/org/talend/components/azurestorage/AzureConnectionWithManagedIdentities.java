//==============================================================================
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
//==============================================================================

package org.talend.components.azurestorage;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageCredentials;
import com.microsoft.azure.storage.StorageCredentialsToken;
import org.talend.components.azure.runtime.token.AzureManagedIdentitiesTokenGetter;

public class AzureConnectionWithManagedIdentities implements AzureConnection {

    private final String accountName;
    private final String endpoint;


    public AzureConnectionWithManagedIdentities(String accountName,String endpoint) {
        this.accountName = accountName;
        this.endpoint = endpoint;
    }

    @Override
    public CloudStorageAccount getCloudStorageAccount() {
        try {
            String token = new AzureManagedIdentitiesTokenGetter().retrieveSystemAssignMItoken();
            StorageCredentials credentials = new StorageCredentialsToken(accountName, token);
            return new CloudStorageAccount(credentials, true,endpoint);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
