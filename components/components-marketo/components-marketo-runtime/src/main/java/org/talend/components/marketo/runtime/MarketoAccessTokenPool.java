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
package org.talend.components.marketo.runtime;

import java.util.HashMap;
import java.util.Map;

public class MarketoAccessTokenPool {

    private static MarketoAccessTokenPool ourInstance = new MarketoAccessTokenPool();

    private Map<Integer, String> tokens;

    public static MarketoAccessTokenPool getInstance() {
        return ourInstance;
    }

    private MarketoAccessTokenPool() {
        tokens = new HashMap<>();
    }

    public void invalidateToken(Integer connectionHash) {
        tokens.remove(connectionHash);
    }

    public void setToken(Integer connectionHash, String token) {
        tokens.put(connectionHash, token);
    }

    public String getToken(Integer connectionHash) {
        return tokens.get(connectionHash);
    }

}
