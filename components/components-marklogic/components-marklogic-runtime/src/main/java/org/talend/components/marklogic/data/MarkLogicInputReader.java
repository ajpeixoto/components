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
package org.talend.components.marklogic.data;

import org.talend.components.api.component.runtime.BoundedSource;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.marklogic.runtime.input.MarkLogicCriteriaReader;

/**
 *
 *
 */
public class MarkLogicInputReader extends MarkLogicCriteriaReader {

    public MarkLogicInputReader(BoundedSource source, RuntimeContainer container, MarkLogicDatasetProperties properties) {
        super(source, container, properties);
    }

    @Override
    protected Setting prepareSettings(ComponentProperties inputProperties) {
        MarkLogicDatasetProperties properties = (MarkLogicDatasetProperties) inputProperties;

        return new Setting(
                properties.main.schema.getValue(),
                properties.criteria.getValue(), -1, properties.pageSize.getValue(),
                properties.useQueryOption.getValue(), properties.queryLiteralType.getValue(),
                properties.queryOptionName.getValue(), properties.queryOptionLiterals.getValue(),
                properties.getDatastoreProperties().isReferencedConnectionUsed());
    }

}
