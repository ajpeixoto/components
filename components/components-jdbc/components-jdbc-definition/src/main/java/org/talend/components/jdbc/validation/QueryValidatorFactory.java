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
