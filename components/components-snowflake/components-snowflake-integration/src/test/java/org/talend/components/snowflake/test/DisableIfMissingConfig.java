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
package org.talend.components.snowflake.test;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.LoggerFactory;

public class DisableIfMissingConfig implements TestRule {
    @Override
    public Statement apply(Statement base, Description description) {
        if (System.getProperty("snowflake.account", "").trim().isEmpty()) {
            return new Statement() {
                @Override
                public void evaluate() {
                    LoggerFactory.getLogger(DisableIfMissingConfig.class)
                                 .warn("Missing system property 'snowflake.account', skipping {}", description);
                }
            };
        }
        return base;
    }
}
