package com.commercetools.emailprocessor.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.client.SphereClientConfig;
import io.sphere.sdk.client.SphereClientFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.net.URL;


@JsonIgnoreProperties(ignoreUnknown = true)
public class TenantConfiguration {


    private static final Logger LOG = LoggerFactory.getLogger(TenantConfiguration.class);

    private SphereClient client = null;
    private String projectKey;
    private String clientId;
    private String clientSecret;
    private String endpointUrl;

    @JsonIgnore
    private HttpURLConnection httpUrlConnection;

    public TenantConfiguration() {
    }

    /**
     * Creates a configuration of a tenant.
     *
     * @param key    ctp project key
     * @param id     ctp project client id
     * @param secret ctp project client secret
     * @param url    api endpoint url
     */
    public TenantConfiguration(final String key, final String id, final String secret, final String url) {
        projectKey = key;
        clientId = id;
        clientSecret = secret;
        endpointUrl = url;
    }



    public String getProjectKey() {
        return projectKey;
    }

    public void setProjectKey(final String projectKey) {
        this.projectKey = projectKey;
    }

    public void setClientId(final String clientId) {
        this.clientId = clientId;
    }

    public void setClientSecret(final String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public void setEndpointUrl(final String url) {
        this.endpointUrl = url;
    }

    public void setClient(final SphereClient client) {
        this.client = client;
    }

    /**
     * Validates the current tenant configuration.
     *
     * @return true, if the configuration is valid
     */
    public boolean isValid() {
        String errorMessage = "Please define the missing Property '%s'";
        boolean isValid = true;

        if (StringUtils.isEmpty(getProjectKey())) {
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

        if (StringUtils.isEmpty(endpointUrl)) {
            LOG.error(String.format(errorMessage, "endpointUrl"));
            isValid = false;
        }
        return isValid;

    }

    /**
     * Create a SphereClient based on the current tenantconfig.
     *
     * @return the current SphereClient
     */
    public SphereClient getSphereClient() {
        if (client == null) {
            SphereClientConfig sphereConfig = SphereClientConfig.of(projectKey, clientId, clientSecret);
            final SphereClientFactory factory = SphereClientFactory.of();
            client = factory.createClient(sphereConfig);
        }

        return client;
    }

    /**
     * Create a HttpURLConnection based on the current tenantconfig.
     *
     * @return the current HttpURLConnection
     * @throws Exception when the HttpURLConnection cannot be created.
     */
    @JsonIgnore
    public HttpURLConnection getHttpUrlConnection() throws Exception {
        if (this.httpUrlConnection == null) {
            URL postUrl = new URL(endpointUrl);
            return (HttpURLConnection) postUrl.openConnection();
        }
        return httpUrlConnection;
    }

    /**
     * Sets a given urlConnection.
     */
    @JsonIgnore
    public void setHttpUrlConnection(final HttpURLConnection httpUrlConnection) {
        this.httpUrlConnection = httpUrlConnection;
    }


}
