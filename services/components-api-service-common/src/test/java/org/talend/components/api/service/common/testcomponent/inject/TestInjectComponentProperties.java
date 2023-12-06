// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.api.service.common.testcomponent.inject;

import jakarta.inject.Inject;
import org.talend.components.api.properties.ComponentPropertiesImpl;
import org.talend.daikon.definition.service.DefinitionRegistryService;

public class TestInjectComponentProperties extends ComponentPropertiesImpl {

    @Inject
    DefinitionRegistryService definitionRegistry;

    public TestInjectComponentProperties(String name) {
        super(name);
    }

    public DefinitionRegistryService getDefinitionRegistry() {
        return definitionRegistry;
    }

}
