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

package org.talend.components.marklogic.runtime.bulkload;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.marklogic.exceptions.MarkLogicErrorCode;
import org.talend.components.marklogic.exceptions.MarkLogicException;
import org.talend.components.marklogic.tmarklogicbulkload.MarkLogicBulkLoadProperties;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;

import com.marklogic.contentpump.ContentPump;

public class MarkLogicInternalBulkLoadRunner extends AbstractMarkLogicBulkLoadRunner {

    private transient static final Logger LOGGER = LoggerFactory.getLogger(MarkLogicInternalBulkLoadRunner.class);

    private static final I18nMessages MESSAGES = GlobalI18N.getI18nMessageProvider().getI18nMessages(MarkLogicBulkLoad.class);

    protected MarkLogicInternalBulkLoadRunner(MarkLogicBulkLoadProperties properties) {
        super(properties);
    }

    @Override
    protected void runBulkLoading(List<String> parameters) {
        LOGGER.debug(MESSAGES.getMessage("messages.debug.command", parameters));
        LOGGER.info(MESSAGES.getMessage("messages.info.startBulkLoad"));
        try {
            ContentPump.runCommand(parameters.toArray(new String[0]));
        } catch (IOException e) {
            String errorMessage = MESSAGES.getMessage("messages.error.exception");
            LOGGER.error(errorMessage, e.getMessage());
            throw new MarkLogicException(new MarkLogicErrorCode(errorMessage), e);
        }

    }
}
