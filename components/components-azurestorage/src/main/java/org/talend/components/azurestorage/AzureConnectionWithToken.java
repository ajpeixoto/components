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

package org.talend.components.azurestorage;

import org.talend.components.azure.runtime.token.AzureActiveDirectoryTokenGetter;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageCredentials;
import com.microsoft.azure.storage.StorageCredentialsToken;

public class AzureConnectionWithToken implements AzureConnection {

    private final String accountName;
    private final AzureActiveDirectoryTokenGetter tokenGetter;
    private final String endpoint;


    public AzureConnectionWithToken(String accountName, String tenantId, String clientId, String clientSecret, String endpoint, String authorityHost) {
        this.accountName = accountName;
        this.tokenGetter = new AzureActiveDirectoryTokenGetter(tenantId, clientId, clientSecret,authorityHost);
        this.endpoint = endpoint;
    }

    //Only for test
    public AzureConnectionWithToken(String accountName, AzureActiveDirectoryTokenGetter tokenGetter, String endpoint) {
        this.accountName = accountName;
        this.tokenGetter = tokenGetter;
        this.endpoint = endpoint;
    }

    @Override
    public CloudStorageAccount getCloudStorageAccount() {
        try {
            String token = tokenGetter.retrieveAccessToken(endpoint);
            StorageCredentials credentials = new StorageCredentialsToken(accountName, token);
            return new CloudStorageAccount(credentials, true,endpoint);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
