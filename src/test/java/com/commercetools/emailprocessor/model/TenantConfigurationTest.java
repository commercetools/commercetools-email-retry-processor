package com.commercetools.emailprocessor.model;

import io.sphere.sdk.client.SphereApiConfig;
import io.sphere.sdk.client.SphereClient;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TenantConfigurationTest {


    private String projectKey = "anyKey";
    private String clientId = "anyID";
    private String clientSecret = "anySecret";
    private String webhookURL = "anyUrl";

    private TenantConfiguration tenantConfig = new TenantConfiguration();

    @Before
    public void setUp() throws Exception {
        tenantConfig = new TenantConfiguration(projectKey, clientId, clientSecret, webhookURL);

    }

    @Test
    public void getProjectKey() throws Exception {
        assertEquals(tenantConfig.getProjectKey(), projectKey);
    }

    @Test
    public void getClientId() throws Exception {
        assertEquals(tenantConfig.getClientId(), clientId);
    }

    @Test
    public void getClientSecret() throws Exception {
        assertEquals(tenantConfig.getClientSecret(), clientSecret);

    }

    @Test
    public void getWebhookURL() throws Exception {
        assertEquals(tenantConfig.getApiEndpointURL(), webhookURL);

    }

    @Test
    public void isValid() throws Exception {
        tenantConfig = new TenantConfiguration(projectKey, clientId, clientSecret, webhookURL);
        assertEquals(tenantConfig.isValid(), true);
    }

    @Test
    public void isNotVali1() throws Exception {
        tenantConfig = new TenantConfiguration(projectKey, clientId, clientSecret, null);
        assertEquals(tenantConfig.isValid(), false);

        tenantConfig = new TenantConfiguration(projectKey, clientId, null, webhookURL);
        assertEquals(tenantConfig.isValid(), false);

        tenantConfig = new TenantConfiguration(projectKey, null, clientSecret, webhookURL);
        assertEquals(tenantConfig.isValid(), false);

        tenantConfig = new TenantConfiguration(null, clientId, clientSecret, webhookURL);
        assertEquals(tenantConfig.isValid(), false);
   
        tenantConfig = new TenantConfiguration(null, null, null, null);
        assertEquals(tenantConfig.isValid(), false);
    }

    @Test
    public void getSphereClient() throws Exception {
        SphereClient client = tenantConfig.getSphereClient();
        SphereApiConfig clientConfig = client.getConfig();
        assertEquals(clientConfig.getProjectKey(), projectKey);
    }
}