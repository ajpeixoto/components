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

package org.talend.components.hadoopcluster.configuration.input;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.daikon.runtime.RuntimeInfo;

public class HadoopClusterConfigurationInputDefinitionTest {

    private final HadoopClusterConfigurationInputDefinition inputDefinition = new HadoopClusterConfigurationInputDefinition();

    @Test
    @Ignore("This can't work unless the runtime jar is already installed in maven!")
    public void testRuntimeInfo() {
        HadoopClusterConfigurationInputProperties properties = new HadoopClusterConfigurationInputProperties("properties");
        properties.init();
        RuntimeInfo runtimeInfo = inputDefinition.getRuntimeInfo(ExecutionEngine.DI, properties, null);
        assertEquals("org.talend.components.hadoopcluster.runtime.configuration.input.ClouderaManagerConfigurationSource",
                runtimeInfo.getRuntimeClassName());
        properties.clusterManagerType.setValue(HadoopClusterConfigurationInputProperties.ClusterManagerType.AMBARI);
        runtimeInfo = inputDefinition.getRuntimeInfo(ExecutionEngine.DI, properties, null);
        assertEquals("org.talend.components.hadoopcluster.runtime.configuration.input.AmbariConfigurationSource",
                runtimeInfo.getRuntimeClassName());
    }

    @Test
    public void testGetPropertyClass() {
        Class<?> propertyClass = inputDefinition.getPropertyClass();
        String canonicalName = propertyClass.getCanonicalName();
        assertThat(canonicalName, equalTo("org.talend.components.hadoopcluster.configuration.input.HadoopClusterConfigurationInputProperties"));
    }

    @Test
    public void testGetName() {
        String componentName = inputDefinition.getName();
        assertEquals(componentName, "HadoopClusterConfigurationInput");
    }

    @Test
    public void testGetSupportedConnectorTopologies() {
        Set<ConnectorTopology> test = inputDefinition.getSupportedConnectorTopologies();
        assertTrue(test.contains(ConnectorTopology.OUTGOING));
    }
}
