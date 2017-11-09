package org.talend.components.marketo.runtime.client.rest.response;

import java.util.ArrayList;
import java.util.List;

import org.talend.components.marketo.runtime.client.rest.type.ActivityType;

public class ActivityTypesResult extends RequestResult {

    private List<ActivityType> result;

    public List<ActivityType> getResult() {
        // ensure that result is never null
        if (result == null) {
            return new ArrayList<>();
        }

        return result;
    }

    public void setResult(List<ActivityType> result) {
        this.result = result;
    }
}
