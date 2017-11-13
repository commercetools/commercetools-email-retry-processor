package com.commercetools.emailprocessor.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.sphere.sdk.client.BlockingSphereClient;
import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.client.SphereClientConfig;
import io.sphere.sdk.client.SphereClientFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;


@JsonIgnoreProperties(ignoreUnknown = true)
public class TenantConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(TenantConfiguration.class);

    private String projectKey;
    private String clientId;
    private String clientSecret;
    private String webhookURL;

    public TenantConfiguration() {
    }

    public TenantConfiguration(String key,String id,String secret,String url) {
        projectKey=key;
        clientId=id;
        clientSecret=secret;
        webhookURL=url;

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

    public String getWebhookURL() {
        return webhookURL;
    }

    public void setWebhookURL(String webhookURL) {
        this.webhookURL = webhookURL;
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

        if (StringUtils.isEmpty(webhookURL)) {
            LOG.error(String.format(errorMessage, "webhookURL"));
            isValid = false;
        }
       return isValid;

    }

    public SphereClient getSphereClient() {
        SphereClientConfig sphereConfig = SphereClientConfig.of(projectKey, clientId, clientSecret);
        final SphereClientFactory factory = SphereClientFactory.of();
        final SphereClient client = factory.createClient(sphereConfig);
        return client;
    }


}
