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
package org.talend.components.salesforce.runtime.dataprep;

import java.io.IOException;
import java.util.List;

import org.apache.avro.Schema;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.salesforce.dataset.SalesforceDatasetProperties;
import org.talend.components.salesforce.runtime.SalesforceBulkExecRuntime;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;

class SalesforceSchemaUtils {

    private final I18nMessages MESSAGES =
            GlobalI18N.getI18nMessageProvider().getI18nMessages(SalesforceSchemaUtils.class);

    public static Schema getSchema(SalesforceDatasetProperties dataset, SalesforceDataprepSource sds, RuntimeContainer container)
            throws IOException {
        return new SalesforceSchemaUtils().guessSchema(dataset, sds, container);
    }

    private Schema guessSchema(SalesforceDatasetProperties dataset, SalesforceDataprepSource sds, RuntimeContainer container)
            throws IOException {
        if (dataset.sourceType.getValue() == SalesforceDatasetProperties.SourceType.MODULE_SELECTION) {
            List<String> fields = dataset.selectColumnIds.getValue();
            if (!fields.isEmpty()) {
                try {
                    return sds.guessSchema(query(sds, dataset, fields));
                } catch (Exception exception) {
                    throw new RuntimeException(MESSAGES.getMessage("error.retrieveSchemaFromSOQL"), exception);
                }
            }
            throw new RuntimeException(MESSAGES.getMessage("error.retrieveSchemaNoField"));
        } else {
            return sds.guessSchema(dataset.query.getValue());
        }
    }

    private String query(SalesforceDataprepSource sds, SalesforceDatasetProperties dataset, List<String> fields)
            throws IOException {
        if (dataset.sourceType.getValue() != SalesforceDatasetProperties.SourceType.MODULE_SELECTION || fields.isEmpty()) {
            throw new IllegalArgumentException(MESSAGES.getMessage("error.moduleAndFieldEmpty"));
        }
        StringBuilder sb = new StringBuilder();
        sb.append("select ");
        int count = 0;
        for (String wantedFieldName : fields) {
            if (count++ > 0) {
                sb.append(", ");
            }
            sb.append(wantedFieldName);
        }
        sb.append(" from ");
        sb.append(dataset.moduleName.getValue());
        return sb.toString();
    }

}
