package com.commercetools.emailprocessor.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.client.SphereClientConfig;
import io.sphere.sdk.client.SphereClientFactory;
import org.apache.http.client.methods.HttpPost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

import static org.apache.commons.lang3.StringUtils.isNotBlank;


@JsonIgnoreProperties(ignoreUnknown = true)
public class TenantConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(TenantConfiguration.class);
    private SphereClient client;
    private String projectKey;
    private String clientId;
    private String clientSecret;
    private String endpointUrl;
    private String encryptionKey;
    private boolean processAll = false;

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
    TenantConfiguration(@Nonnull final String key, @Nonnull final String id, @Nonnull final String secret,
                        @Nonnull final String url, @Nonnull final String encryption, boolean all) {
        projectKey = key;
        clientId = id;
        clientSecret = secret;
        endpointUrl = url;
        encryptionKey = encryption;
        processAll = all;
    }

    public String getProjectKey() {
        return projectKey;
    }

    public void setProjectKey(final String projectKey) {
        this.projectKey = projectKey;
    }

    public void setClient(final SphereClient client) {
        this.client = client;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(final String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(final String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }

    public void setEndpointUrl(final String url) {
        this.endpointUrl = url;
    }

    public String getEncryptionKey() {
        return encryptionKey;
    }

    public void setEncryptionKey(final String encryptionKey) {
        this.encryptionKey = encryptionKey;
    }

    public boolean isProcessAll() {
        return processAll;
    }

    public void setProcessAll(boolean processAll) {
        this.processAll = processAll;
    }

    /**
     * Validates the current tenant configuration.
     *
     * @return true, if the configuration is valid
     */
    boolean isValid() {
        return isNotBlank(projectKey) && isNotBlank(clientId) && isNotBlank(clientSecret) && isNotBlank(endpointUrl)
            && isNotBlank(encryptionKey);
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
     */
    @JsonIgnore
    public HttpPost getHttpPost() {
        if (this.httpPost == null) {
            return new HttpPost(endpointUrl);
        }
        return httpPost;
    }
}