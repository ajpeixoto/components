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
package org.talend.components.azurestorage.queue.tazurestoragequeuepurge;

import java.util.EnumSet;
import java.util.Set;

import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.azurestorage.queue.AzureStorageQueueDefinition;
import org.talend.components.azurestorage.queue.runtime.AzureStorageQueuePurgeRuntime;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.runtime.RuntimeInfo;

public class TAzureStorageQueuePurgeDefinition extends AzureStorageQueueDefinition {

    public static final String COMPONENT_NAME = "tAzureStorageQueuePurge";

    public TAzureStorageQueuePurgeDefinition() {
        super(COMPONENT_NAME);
    }

    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return TAzureStorageQueuePurgeProperties.class;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Property[] getReturnProperties() {
        return new Property[] { RETURN_ERROR_MESSAGE_PROP, RETURN_QUEUE_NAME_PROP };
    }

    @Override
    public RuntimeInfo getRuntimeInfo(ExecutionEngine engine, ComponentProperties properties,
            ConnectorTopology connectorTopology) {

        assertConnectorTopologyCompatibility(connectorTopology);
        assertEngineCompatibility(engine);
        return getCommonRuntimeInfo(this.getClass().getClassLoader(), AzureStorageQueuePurgeRuntime.class);
    }

    @Override
    public Set<ConnectorTopology> getSupportedConnectorTopologies() {
        return EnumSet.of(ConnectorTopology.NONE);
    }
}
