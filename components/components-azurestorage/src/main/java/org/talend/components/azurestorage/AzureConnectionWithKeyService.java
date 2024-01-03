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

import java.net.URISyntaxException;
import java.security.InvalidKeyException;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageCredentialsAccountAndKey;
import org.apache.commons.lang3.StringUtils;

/**
 * This class hold and provide azure storage connection using a key
 */
public class AzureConnectionWithKeyService implements AzureConnection {

    private String protocol;

    private String accountName;

    private String accountKey;

    private String endpoint;

    AzureConnectionWithKeyService(Builder builder) {
        this.protocol = builder.protocol;
        this.accountName = builder.accountName;
        this.accountKey = builder.accountKey;
        this.endpoint = builder.endpoint;
    }

    @Override
    public CloudStorageAccount getCloudStorageAccount() throws InvalidKeyException, URISyntaxException {
        final StorageCredentialsAccountAndKey storageCredentials = new StorageCredentialsAccountAndKey(accountName,accountKey);
        CloudStorageAccount cloudStorageAccount = new CloudStorageAccount(storageCredentials, true, endpoint);
        return cloudStorageAccount;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getAccountName() {
        return accountName;
    }

    public String getAccountKey() {
        return accountKey;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder  {

        private String protocol;

        private String accountName;

        private String accountKey;

        private String endpoint;

        public Builder protocol(String protocol) {
            this.protocol = protocol;
            return this;
        }

        public Builder accountName(String accountName) {
            this.accountName = accountName;
            return this;
        }

        public Builder accountKey(String accountKey) {
            this.accountKey = accountKey;
            return this;
        }

        public Builder withEndpoint(String endpoint) {
            if (!StringUtils.isEmpty(endpoint)){
                this.endpoint = endpoint;
            }
            return this;
        }

        public AzureConnectionWithKeyService build() {
            return new AzureConnectionWithKeyService(this);
        }
    }



}
