package com.commercetools.emailprocessor.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.client.SphereClientConfig;
import io.sphere.sdk.client.SphereClientFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.Transient;
import java.net.HttpURLConnection;
import java.net.URL;


@JsonIgnoreProperties(ignoreUnknown = true)
public class TenantConfiguration {


    private static final Logger LOG = LoggerFactory.getLogger(TenantConfiguration.class);
    private SphereClient client = null;
    private String projectKey;
    private String clientId;
    private String clientSecret;
    private String apiEndpointURL;

    @JsonIgnore
    private HttpURLConnection httpURLConnection;

    public TenantConfiguration() {
    }


    public TenantConfiguration(String key, String id, String secret, String url) {
        projectKey = key;
        clientId = id;
        clientSecret = secret;
        apiEndpointURL = url;

    }

    public String getProjectKey() {
        return projectKey;
    }

    public void setProjectKey(String projectKey) {
        this.projectKey = projectKey;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getApiEndpointURL() {
        return apiEndpointURL;
    }

    public void setApiEndpointURL(String apiEndpointURL) {
        this.apiEndpointURL = apiEndpointURL;
    }

    public void setClient(SphereClient client) {
        this.client = client;
    }

    public boolean isValid() {
        String errorMessage = "Please define the missing Property '%s'";
        boolean isValid = true;

        if (StringUtils.isEmpty(projectKey)) {
            LOG.error(String.format(errorMessage, "projectKey"));
            isValid = false;
        }
        if (StringUtils.isEmpty(clientId)) {
            LOG.error(String.format(errorMessage, "clientId"));
            isValid = false;
        }
        if (StringUtils.isEmpty(clientSecret)) {
            LOG.error(String.format(errorMessage, "clientSecret"));
            isValid = false;
        }

        if (StringUtils.isEmpty(apiEndpointURL)) {
            LOG.error(String.format(errorMessage, "apiEndpointURL"));
            isValid = false;
        }
        return isValid;

    }

    public SphereClient getSphereClient() {
        if (client == null) {
            SphereClientConfig sphereConfig = SphereClientConfig.of(projectKey, clientId, clientSecret);
            final SphereClientFactory factory = SphereClientFactory.of();
            client = factory.createClient(sphereConfig);
        }

        return client;
    }

    @JsonIgnore
    public HttpURLConnection getHttpURLConnection() throws Exception {
        if (this.httpURLConnection == null) {
            URL postUrl = new URL(apiEndpointURL);
            return (HttpURLConnection) postUrl.openConnection();

        }
        return httpURLConnection;
    }

    public void setHttpURLConnection(HttpURLConnection httpURLConnection) {
        this.httpURLConnection = httpURLConnection;
    }

}
