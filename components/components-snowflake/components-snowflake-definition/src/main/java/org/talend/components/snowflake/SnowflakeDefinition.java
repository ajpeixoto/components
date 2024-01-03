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
package org.talend.components.snowflake;

import org.talend.components.api.component.AbstractComponentDefinition;
import org.talend.components.api.component.runtime.DependenciesReader;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.api.component.runtime.JarRuntimeInfo;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.runtime.RuntimeInfo;
import org.talend.daikon.runtime.RuntimeUtil;
import org.talend.daikon.sandbox.SandboxedInstance;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * The SnowflakeDefinition acts as an entry point for all of services that
 * a component provides to integrate with the Studio (at design-time) and other
 * components (at run-time).
 */
public abstract class SnowflakeDefinition extends AbstractComponentDefinition {

    public static final String RUNTIME_MVN_URL = "mvn:org.talend.components/components-snowflake-runtime";

    public static final String RUNTIME_MVN_GROUP_ID = "org.talend.components";

    public static final String DEFINITION_MVN_ARTIFACT_ID = "components-snowflake-definition";
    public static final String RUNTIME_MVN_ARTIFACT_ID = "components-snowflake-runtime";

    public static final String SOURCE_CLASS = "org.talend.components.snowflake.runtime.SnowflakeSource";

    public static final String SINK_CLASS = "org.talend.components.snowflake.runtime.SnowflakeSink";

    public static final String SOURCE_OR_SINK_CLASS = "org.talend.components.snowflake.runtime.SnowflakeSourceOrSink";

    public static final String ROW_SINK_CLASS = "org.talend.components.snowflake.runtime.SnowflakeRowSink";

    public static final String ROW_STANDALONE_CLASS = "org.talend.components.snowflake.runtime.SnowflakeRowStandalone";

    public static final String ROW_SOURCE_CLASS = "org.talend.components.snowflake.runtime.SnowflakeRowSource";

    public static final String CLOSE_SOURCE_OR_SINK_CLASS =
            "org.talend.components.snowflake.runtime.SnowflakeCloseSourceOrSink";

    public static final String COMMIT_SOURCE_OR_SINK_CLASS =
            "org.talend.components.snowflake.runtime.SnowflakeCommitSourceOrSink";

    public static final String ROLLBACK_SOURCE_OR_SINK_CLASS =
            "org.talend.components.snowflake.runtime.SnowflakeRollbackSourceOrSink";

    public static final boolean USE_CURRENT_JVM_PROPS = true;

    /** Provides {@link SandboxedInstance}s. */
    private static SandboxedInstanceProvider sandboxedInstanceProvider = SandboxedInstanceProvider.INSTANCE;

    public SnowflakeDefinition(String componentName) {
        super(componentName, ExecutionEngine.DI);
    }

    @Override
    public String[] getFamilies() {
        return new String[] { "Cloud/Snowflake" }; //$NON-NLS-1$
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends ComponentProperties>[] getNestedCompatibleComponentPropertiesClass() {
        return new Class[] { SnowflakeConnectionProperties.class };
    }

    @Override
    public Property<?>[] getReturnProperties() {
        return new Property[] { RETURN_ERROR_MESSAGE_PROP, RETURN_TOTAL_RECORD_COUNT_PROP };
    }

    public static RuntimeInfo getCommonRuntimeInfo(String clazzFullName) {
        return getCommonRuntimeInfo(RUNTIME_MVN_URL, clazzFullName);
    }

    public static RuntimeInfo getCommonRuntimeInfo(String runtimeMvnUrl, String clazzFullName) {
        return new JarRuntimeInfo(runtimeMvnUrl,
                DependenciesReader.computeDependenciesFilePath(RUNTIME_MVN_GROUP_ID, RUNTIME_MVN_ARTIFACT_ID),
                clazzFullName);
    }

    /**
     * Get {@link SandboxedInstance} for given runtime object class.
     *
     * @see SandboxedInstanceProvider
     *
     * @param runtimeClassName full name of runtime object class
     * @param useCurrentJvmProperties whether to use current JVM properties
     * @return sandboxed instance
     */
    public static SandboxedInstance getSandboxedInstance(String runtimeClassName, boolean useCurrentJvmProperties) {
        return sandboxedInstanceProvider.getSandboxedInstance(runtimeClassName, useCurrentJvmProperties);
    }

    /**
     * Set provider of {@link SandboxedInstance}s.
     *
     * <p>
     * The method is intended for debug/test purposes only and should not be used in production.
     *
     * @param provider provider to be set, can't be {@code null}
     */
    public static void setSandboxedInstanceProvider(SandboxedInstanceProvider provider) {
        sandboxedInstanceProvider = provider;
    }

    /**
     * Provides {@link SandboxedInstance} objects.
     */
    public static class SandboxedInstanceProvider {

        /** Shared instance of provider. */
        public static final SandboxedInstanceProvider INSTANCE = new SandboxedInstanceProvider();

        /**
         * Get {@link SandboxedInstance} for given runtime object class.
         *
         * @param runtimeClassName full name of runtime object class
         * @param useCurrentJvmProperties whether to use current JVM properties
         * @return sandboxed instance
         */
        public SandboxedInstance getSandboxedInstance(final String runtimeClassName,
                final boolean useCurrentJvmProperties) {
            ClassLoader classLoader = SnowflakeDefinition.class.getClassLoader();
            final String definitionVersion = getDefinitionVersion();
            final RuntimeInfo runtimeInfo;
            if(definitionVersion != null) {
                //definition version should be the same with runtime version
                runtimeInfo = SnowflakeDefinition.getCommonRuntimeInfo(RUNTIME_MVN_URL + "/" + definitionVersion, runtimeClassName);
            } else {
                runtimeInfo = SnowflakeDefinition.getCommonRuntimeInfo(runtimeClassName);
            }
            if (useCurrentJvmProperties) {
                return RuntimeUtil.createRuntimeClassWithCurrentJVMProperties(runtimeInfo, classLoader);
            } else {
                return RuntimeUtil.createRuntimeClass(runtimeInfo, classLoader);
            }
        }

        private String getDefinitionVersion() {
            String version = null;
            final String dependenciesFilePath = "/META-INF/maven/" + RUNTIME_MVN_GROUP_ID + "/" + DEFINITION_MVN_ARTIFACT_ID + "/dependencies.txt";
            try(final BufferedReader reader = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream(dependenciesFilePath), "UTF-8"));) {
                String line = null;
                while (reader.ready()) {
                    line = reader.readLine();
                }
                if(line!=null && line.contains(DEFINITION_MVN_ARTIFACT_ID)) {
                    version = line.split(":")[3];
                }
            } catch (Exception e) {

            }
            return version;
        }
    }
}
