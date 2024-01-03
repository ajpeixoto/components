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
package org.talend.components.hadoopcluster.runtime.configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cloudera.api.swagger.ServicesResourceApi;
import com.cloudera.api.swagger.client.ApiException;
import com.cloudera.api.swagger.model.ApiService;
import com.cloudera.api.swagger.model.ApiServiceList;

public class HadoopCMCluster implements HadoopCluster {

    static final String DEFAULT_VIEW_NAME = "summary";

    ServicesResourceApi serviceAPI;
    String clusterName;
    
    List<String> blacklistParams;

    public HadoopCMCluster(ServicesResourceApi serviceAPI, String clusterName) {
        this.serviceAPI = serviceAPI;
        this.clusterName = clusterName;
    }

    @Override
    public Map<HadoopHostedService, HadoopClusterService> getHostedServices() {
        Map<HadoopHostedService, HadoopClusterService> servicesMapping = new HashMap<HadoopHostedService, HadoopClusterService>();
        ApiServiceList services;
        try {
            services = this.serviceAPI.readServices(this.clusterName, DEFAULT_VIEW_NAME);
            for (ApiService service : services.getItems()) {
                if (HadoopHostedService.isSupport(service.getType())) {
                    HadoopCMClusterService clusterService = new HadoopCMClusterService(this.clusterName, service.getName(),
                            this.serviceAPI,
                            blacklistParams);
                    if (clusterService.hasConfigurations()) {
                        servicesMapping.put(HadoopHostedService.fromString(service.getType()), clusterService);
                    }
                }
            }
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
        return servicesMapping;
    }

	@Override
	public void setBlacklistParams(List<String> names) {
		blacklistParams = names;
	}
}
