package com.commercetools.emailprocessor.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.client.SphereClientConfig;
import io.sphere.sdk.client.SphereClientFactory;
import org.apache.http.client.methods.HttpPost;

import javax.annotation.Nonnull;

import static org.apache.commons.lang3.StringUtils.isNotBlank;


@JsonIgnoreProperties(ignoreUnknown = true)
public class TenantConfiguration {

    private SphereClient client;
    private String projectKey;
    private String clientId;
    private String clientSecret;
    private String endpointUrl;
    private String encryptionKey;
    private boolean processAll = false;
    private Long queryLimit = 100L;
    @JsonIgnore
    private HttpPost httpPost;

    // TODO: only for tests now, should be removed then
    public TenantConfiguration() {
    }

    // TODO: only for tests now, should be removed then

    /**
     * Creates a configuration of a tenant.
     *
     * @param projectKey   ctp project key
     * @param clientId     ctp project client id
     * @param clientSecret ctp project client secret
     * @param endpointUrl  api endpoint url
     */
    TenantConfiguration(@Nonnull final String projectKey,
                        @Nonnull final String clientId,
                        @Nonnull final String clientSecret,
                        @Nonnull final String endpointUrl,
                        @Nonnull final String encryptionKey,
                        boolean all,
                        final Long limit) {
        this.projectKey = projectKey;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.endpointUrl = endpointUrl;
        this.encryptionKey = encryptionKey;
        processAll = all;
        queryLimit = limit;
    }

    @JsonCreator
    TenantConfiguration(@JsonProperty("projectKey") @Nonnull final String projectKey,
                        @JsonProperty("clientId") @Nonnull final String clientId,
                        @JsonProperty("clientSecret") @Nonnull final String clientSecret,
                        @JsonProperty("endpointUrl") @Nonnull final String endpointUrl,
                        @JsonProperty("encryptionKey") @Nonnull final String encryptionKey) {
        this(projectKey, clientId, clientSecret, endpointUrl, encryptionKey, false, 100L);
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

    public Long getQueryLimit() {
        return queryLimit;
    }

    public void setQueryLimit(final Long queryLimit) {
        this.queryLimit = queryLimit;
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