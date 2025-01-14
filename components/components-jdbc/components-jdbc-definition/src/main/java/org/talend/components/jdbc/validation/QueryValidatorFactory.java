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
package org.talend.components.jdbc.validation;

public class QueryValidatorFactory {

    public static enum ValidationType {
        PATTERN,
        CALCITE;
    }

    public static QueryValidator createValidator(final ValidationType validationType) {
        switch (validationType) {
        case CALCITE:
            throw new RuntimeException("Don't support calcite sql check, please use pattern one.");
        default:
            return new PatternQueryValidator();
        }
    }

}
