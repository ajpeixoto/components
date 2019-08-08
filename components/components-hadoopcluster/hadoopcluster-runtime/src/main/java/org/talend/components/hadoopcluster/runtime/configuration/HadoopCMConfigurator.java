// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.hadoopcluster.runtime.configuration;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.logging.Level;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.cloudera.api.swagger.ClustersResourceApi;
import com.cloudera.api.swagger.ServicesResourceApi;
import com.cloudera.api.swagger.client.ApiClient;
import com.cloudera.api.swagger.client.ApiException;
import com.cloudera.api.swagger.client.Configuration;
import com.cloudera.api.swagger.model.ApiCluster;
import com.cloudera.api.swagger.model.ApiClusterList;


public class HadoopCMConfigurator implements HadoopConfigurator {

    static final String CERT_BEGIN = "-----BEGIN CERTIFICATE-----";
    static final String CERT_END = "-----END CERTIFICATE-----";
    static final char SEPARATOR = '\n';
    static final String API_VERSION = "v18";

    ServicesResourceApi serviceAPI;

    ClustersResourceApi clusterAPI;

    public HadoopCMConfigurator(Builder build) {
        ApiClient apiClient;
        try {
            apiClient = createClient(build);
            serviceAPI = new ServicesResourceApi(apiClient);
            clusterAPI = new ClustersResourceApi(apiClient);
        } catch (CertificateEncodingException e) {
            throw new RuntimeException(e);
        }
    }
    
    private ApiClient createClient(Builder build) throws CertificateEncodingException {
        ApiClient cmClient = Configuration.getDefaultApiClient();

        StringBuffer sb = new StringBuffer(build.url.toString());
        if (!sb.toString().endsWith("/")) {
            sb.append("/");
        }
        sb.append("api/");
        sb.append(API_VERSION);

        cmClient.setBasePath(sb.toString());
        cmClient.setUsername(build.user);
        cmClient.setPassword(build.password);

        StringBuffer caCerts = new StringBuffer();
        for (TrustManager tm : build.tms) {
            if (tm instanceof X509TrustManager) {
                X509TrustManager xtm = (X509TrustManager) tm;
                buildCaCerts(caCerts, xtm);
            }
        }

        if (caCerts.length() > 0) {
            cmClient.setVerifyingSsl(true);
            cmClient.setSslCaCert(new ByteArrayInputStream(caCerts.toString().getBytes()));
        }
        return cmClient;
    }

    private void buildCaCerts(StringBuffer caCerts, X509TrustManager xtm) throws CertificateEncodingException {
        if (xtm != null && xtm.getAcceptedIssuers().length > 0) {
            for (Certificate ca : xtm.getAcceptedIssuers()) {
                caCerts.append(CERT_BEGIN);
                caCerts.append(SEPARATOR);
                caCerts.append(Base64.getEncoder().encodeToString(ca.getEncoded()));
                caCerts.append(SEPARATOR);
                caCerts.append(CERT_END);
                caCerts.append(SEPARATOR);
            }
        }
    }

    @Override
    public List<String> getAllClusters() {
        List<String> names = new ArrayList<String>();
        ApiClusterList clusters;
        try {
            clusters = clusterAPI.readClusters(null, HadoopCMCluster.DEFAULT_VIEW_NAME);
            java.util.logging.Logger.getLogger(this.getClass().getCanonicalName()).log(Level.FINEST, clusters.toString());
            for (ApiCluster cluster : clusters.getItems()) {
                names.add(cluster.getDisplayName() + HadoopConfigurator.NAME_SEPARATOR + cluster.getName());
            }
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
        java.util.logging.Logger.getLogger(this.getClass().getCanonicalName()).log(Level.FINEST, names.toString());
        return names;
    }

    @Override
    public HadoopCluster getCluster(String name) {
        java.util.logging.Logger.getLogger(this.getClass().getCanonicalName()).log(Level.FINEST, "clusterName: " + name);
        return new HadoopCMCluster(this.serviceAPI, getClusterName(name));
    }

    private static String getClusterName(String displayNameWithName) {
        if (displayNameWithName == null) {
            return "";
        }
        String[] names = displayNameWithName.split(NAME_SEPARATOR_PATTERN);
        if (names.length > 1) {
            return names[1];
        }
        return names[0];
    }

    public static class Builder implements HadoopConfigurator.Builder {

        private URL url;

        private String user;

        private String password;

        private TrustManager[] tms;

        public Builder() {
        }

        @Override
        public Builder withUrl(URL url) {
            this.url = url;
            return this;
        }

        @Override
        public Builder withUsernamePassword(String user, String password) {
            this.user = user;
            this.password = password;
            return this;
        }

        @Override
        public Builder withTrustManagers(TrustManager[] tms) {
            this.tms = tms;
            return this;
        }

        @Override
        public HadoopCMConfigurator build() {
            return new HadoopCMConfigurator(this);
        }

    }

}
