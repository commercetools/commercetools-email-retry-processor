package com.commercetools.emailprocessor.model;

import io.sphere.sdk.client.SphereApiConfig;
import io.sphere.sdk.client.SphereClient;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TenantConfigurationTest {


    private String projectKey = "anyKey";
    private String clientId = "anyID";
    private String clientSecret = "anySecret";
    private String endpointUrl = "anyUrl";
    private String secretKey = "anySecretKey";
    private TenantConfiguration tenantConfig = new TenantConfiguration();

    @Before
    public void setUp() throws Exception {
        tenantConfig = new TenantConfiguration(projectKey, clientId, clientSecret, endpointUrl, secretKey);

    }

    @Test
    public void isValid() throws Exception {
        tenantConfig = new TenantConfiguration(projectKey, clientId, clientSecret, endpointUrl, secretKey);
        assertEquals(tenantConfig.isValid(), true);
    }

    @Test
    public void isNotValid() throws Exception {
        tenantConfig = new TenantConfiguration(projectKey, clientId, clientSecret, endpointUrl, null);
        assertEquals(tenantConfig.isValid(), false);

        tenantConfig = new TenantConfiguration(projectKey, clientId, clientSecret, null, secretKey);
        assertEquals(tenantConfig.isValid(), false);

        tenantConfig = new TenantConfiguration(projectKey, clientId, null, endpointUrl, secretKey);
        assertEquals(tenantConfig.isValid(), false);

        tenantConfig = new TenantConfiguration(projectKey, null, clientSecret, endpointUrl, secretKey);
        assertEquals(tenantConfig.isValid(), false);

        tenantConfig = new TenantConfiguration(null, clientId, clientSecret, endpointUrl, secretKey);
        assertEquals(tenantConfig.isValid(), false);

        tenantConfig = new TenantConfiguration(null, null, null, null, null);
        assertEquals(tenantConfig.isValid(), false);
    }

    @Test
    public void getSphereClient() throws Exception {
        final SphereClient client = tenantConfig.getSphereClient();
        final SphereApiConfig clientConfig = client.getConfig();
        assertEquals(clientConfig.getProjectKey(), projectKey);
    }
}