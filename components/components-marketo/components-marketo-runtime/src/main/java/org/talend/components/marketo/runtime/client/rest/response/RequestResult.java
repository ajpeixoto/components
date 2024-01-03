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
package org.talend.components.marketo.runtime.client.rest.response;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.talend.components.marketo.runtime.client.type.MarketoError;

public abstract class RequestResult {

    String requestId;

    boolean success;

    boolean moreResult;

    List<MarketoError> errors;

    public String getRequestId() {
        return requestId;
    }

    public boolean isSuccess() {
        return success;
    }

    public List<MarketoError> getErrors() {
        // ensure that errors is never null
        if (errors == null) {
            return new ArrayList<>();
        }
        return errors;
    }

    public String getErrorsString() {
        StringBuilder errs = new StringBuilder("");
        for (MarketoError err : getErrors()) {
            errs.append("{");
            if (!StringUtils.isEmpty(err.getCode())) {
                errs.append("[").append(err.getCode()).append("] ");
            }
            errs.append(err.getMessage());
            errs.append("}");
        }
        return errs.toString();
    }

    public abstract List<?> getResult();

    public boolean isMoreResult() {
        return moreResult;
    }

    public void setMoreResult(boolean moreResults) {
        this.moreResult = moreResults;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setErrors(List<MarketoError> errors) {
        this.errors = errors;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer(getClass().getSimpleName());
        sb.append("{requestId='").append(requestId).append('\'');
        sb.append(", success=").append(success);
        sb.append(", errors=").append(errors);
        sb.append(", result=").append(getResult());
        sb.append(", moreResult=").append(moreResult);
        sb.append('}');
        return sb.toString();
    }
}
