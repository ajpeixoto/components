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
package org.talend.components.azurestorage.blob.runtime;

import com.microsoft.azure.storage.CloudStorageAccount;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.RuntimableRuntime;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.azure.runtime.token.EndpointUtil;
import org.talend.components.azurestorage.AzureConnection;
import org.talend.components.azurestorage.AzureConnectionWithKeyService;
import org.talend.components.azurestorage.AzureConnectionWithManagedIdentities;
import org.talend.components.azurestorage.AzureConnectionWithSasService;
import org.talend.components.azurestorage.AzureConnectionWithToken;
import org.talend.components.azurestorage.AzureStorageProvideConnectionProperties;
import org.talend.components.azurestorage.tazurestorageconnection.AuthType;
import org.talend.components.azurestorage.tazurestorageconnection.TAzureStorageConnectionProperties;
import org.talend.components.azurestorage.utils.AzureStorageUtils;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;
import org.talend.daikon.properties.ValidationResult;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AzureStorageRuntime implements RuntimableRuntime<ComponentProperties> {

    private static Logger LOGGER = LoggerFactory.getLogger(AzureStorageRuntime.class);

    private static final long serialVersionUID = 8150539704549116311L;

    public static final String KEY_CONNECTION_PROPERTIES = "connection";

    public AzureStorageProvideConnectionProperties properties;

    private static final I18nMessages messages = GlobalI18N.getI18nMessageProvider().getI18nMessages(AzureStorageRuntime.class);

    private static final String SAS_PATTERN = "(http.?)?://(.*)\\.(blob|file|queue|table)\\.(.*)/(.*)";

    @Override
    public ValidationResult initialize(RuntimeContainer runtimeContainer, ComponentProperties properties) {
        // init
        this.properties = (AzureStorageProvideConnectionProperties) properties;
        TAzureStorageConnectionProperties conn = getUsedConnection(runtimeContainer);

        if (runtimeContainer != null) {
            AzureStorageUtils.setApplicationVersion((String) runtimeContainer.getGlobalData(AzureStorageUtils.TALEND_PRODUCT_VERSION_GLOBAL_KEY));
            AzureStorageUtils.setComponentVersion((String) runtimeContainer.getGlobalData(AzureStorageUtils.TALEND_COMPONENT_VERSION_GLOBAL_KEY));
        }

        // Validate connection properties

        String errorMessage = "";
        if (conn == null) { // check connection failure

            errorMessage = messages.getMessage("error.VacantConnection"); //$NON-NLS-1$

        } else if (conn.authenticationType.getValue() == AuthType.ACTIVE_DIRECTORY_CLIENT_CREDENTIAL) {
            if (StringUtils.isEmpty(conn.accountName.getValue()) || StringUtils.isEmpty(conn.tenantId.getValue())
                    || StringUtils.isEmpty(conn.clientId.getValue()) || StringUtils.isEmpty(conn.clientSecret.getValue())) {
                errorMessage = messages.getMessage("error.EmptyADProperties");
            }
        } else if (conn.authenticationType.getValue() == AuthType.MANAGED_IDENTITIES){

        } else if (conn.useSharedAccessSignature.getValue()) { // checks
            if (StringUtils.isEmpty(conn.sharedAccessSignature.getStringValue())) {
                errorMessage = messages.getMessage("error.EmptySAS"); //$NON-NLS-1$
            } else {
                Matcher m = Pattern.compile(SAS_PATTERN).matcher(conn.sharedAccessSignature.getValue());
                if (!m.matches()) {
                    errorMessage = messages.getMessage("error.InvalidSAS");
                }
            }

        } else if (!conn.useSharedAccessSignature.getValue() && (StringUtils.isEmpty(conn.accountName.getStringValue())
                || StringUtils.isEmpty(conn.accountKey.getStringValue()))) { // checks connection's account and key

            errorMessage = messages.getMessage("error.EmptyKey"); //$NON-NLS-1$
        }

        // Return result
        if (errorMessage.isEmpty()) {
            return ValidationResult.OK;
        } else {
            return new ValidationResult(ValidationResult.Result.ERROR, errorMessage);
        }
    }

    public TAzureStorageConnectionProperties getConnectionProperties() {
        return properties.getConnectionProperties();
    }

    public TAzureStorageConnectionProperties getUsedConnection(RuntimeContainer runtimeContainer) {
        TAzureStorageConnectionProperties connectionProperties = properties.getConnectionProperties();
        String refComponentId = connectionProperties.getReferencedComponentId();
        LOGGER.debug("RefComponentId: "+ refComponentId);

        // Using another component's connection
        if (refComponentId != null) {
            // In a runtime container
            if (runtimeContainer != null) {
                LOGGER.debug("Runtime container id: "+runtimeContainer.getCurrentComponentId());
                TAzureStorageConnectionProperties sharedConn = (TAzureStorageConnectionProperties) runtimeContainer
                        .getComponentData(refComponentId, KEY_CONNECTION_PROPERTIES);
                LOGGER.debug("sharedConn: " +sharedConn.name.getValue());
                if (sharedConn != null) {
                    return sharedConn;
                }
            }
            // Design time
            connectionProperties = connectionProperties.getReferencedConnectionProperties();
        }
        if (runtimeContainer != null) {
            runtimeContainer.setComponentData(runtimeContainer.getCurrentComponentId(), KEY_CONNECTION_PROPERTIES,
                    connectionProperties);
        }
        return connectionProperties;
    }

    public CloudStorageAccount getStorageAccount(RuntimeContainer runtimeContainer)
            throws URISyntaxException, InvalidKeyException {

        return getAzureConnection(runtimeContainer).getCloudStorageAccount();
    }

    public AzureConnection getAzureConnection(RuntimeContainer runtimeContainer) {

        TAzureStorageConnectionProperties conn = getUsedConnection(runtimeContainer);
        String endpoint  = EndpointUtil.getEndpoint(conn.region.getValue().toString(),conn.customEndpoint.getValue());
        if (conn.authenticationType.getValue() == AuthType.BASIC) {
            if (conn.useSharedAccessSignature.getValue()) {
                // extract account name and sas token from sas url
                Matcher m = Pattern.compile(SAS_PATTERN).matcher(conn.sharedAccessSignature.getValue());
                m.matches();

                return AzureConnectionWithSasService.builder()//
                        .accountName(m.group(2))//
                        .sasToken(m.group(5))//
                        .endpointSuffix(m.group(4))
                        .build();

            } else {

                return AzureConnectionWithKeyService.builder()//
                        .protocol(conn.protocol.getValue().toString().toLowerCase())//
                        .accountName(conn.accountName.getValue())//
                        .accountKey(conn.accountKey.getValue())
                        .withEndpoint(endpoint).build();

            }

        } else if(conn.authenticationType.getValue() == AuthType.ACTIVE_DIRECTORY_CLIENT_CREDENTIAL){
            String authorityHost = conn.region.getValue().equals(TAzureStorageConnectionProperties.Region.CUSTOM)?conn.authorityHost.getValue():null;
            return new AzureConnectionWithToken(conn.accountName.getValue(),
                    conn.tenantId.getValue(), conn.clientId.getValue(), conn.clientSecret.getValue(),
                    endpoint, authorityHost);
        }else {//AuthType.MANAGED_IDENTITIES
            return new AzureConnectionWithManagedIdentities(conn.accountName.getValue(),endpoint);
        }
    }
}
