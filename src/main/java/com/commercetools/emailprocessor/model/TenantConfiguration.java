package com.commercetools.emailprocessor.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.client.SphereClientConfig;
import io.sphere.sdk.client.SphereClientFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpPost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@JsonIgnoreProperties(ignoreUnknown = true)
public class TenantConfiguration implements Cloneable {


    private static final Logger LOG = LoggerFactory.getLogger(TenantConfiguration.class);

    private SphereClient client;
    private String projectKey;
    private String clientId;
    private String clientSecret;
    private String endpointUrl;
    private String encryptionKey;

    @JsonIgnore
    private HttpPost httpPost;

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
    public TenantConfiguration(final String key, final String id, final String secret, final String url,final String
        encryption) {
        projectKey = key;
        clientId = id;
        clientSecret = secret;
        endpointUrl = url;
        encryptionKey = encryption;
    }

    public TenantConfiguration clone() throws CloneNotSupportedException {
        return (TenantConfiguration) super.clone();

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


    public String getEncryptionKey() {
        return encryptionKey;
    }

    public void setEncryptionKey(final String encryptionKey) {
        this.encryptionKey = encryptionKey;
    }

    /**
     * Validates the current tenant configuration.
     *
     * @return true, if the configuration is valid
     */
    boolean isValid() {
        final String errorMessage = "Please define the missing Property '%s'";
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
        if (StringUtils.isEmpty(endpointUrl)) {
            LOG.error(String.format(errorMessage, "endpointUrl"));
            isValid = false;
        }
        if (StringUtils.isEmpty(encryptionKey)) {
            LOG.error(String.format(errorMessage, "encryptionKey"));
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
            client = SphereClientFactory.of().createClient(SphereClientConfig.of(projectKey, clientId, clientSecret));
        }
        return client;
    }

    /**
     * Create a httpPost based on the current tenantconfig.
     *
     * @return the current httpPost
     * @throws Exception when the HttpURLConnection cannot be created.
     */
    @JsonIgnore
    public HttpPost getHttpPost() {
        if (this.httpPost == null) {
            return new HttpPost(endpointUrl);
        }
        return httpPost;
    }

    /**
     * Sets a given HttpPost.
     */
    @JsonIgnore
    public void setHttpPost(final HttpPost httpPost) {
        this.httpPost = httpPost;
    }
}
