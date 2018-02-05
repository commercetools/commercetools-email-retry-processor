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
    private TenantConfiguration tenantConfig;

    @Before
    public void setUp() throws Exception {
        tenantConfig = new TenantConfiguration(projectKey, clientId, clientSecret, endpointUrl, secretKey, false, 100L);
    }

    @Test
    public void isValid() throws Exception {
        tenantConfig = new TenantConfiguration(projectKey, clientId, clientSecret, endpointUrl, secretKey, false, 100L);
        assertEquals(tenantConfig.isValid(), true);
        tenantConfig = new TenantConfiguration(projectKey, clientId, clientSecret, endpointUrl, secretKey, false, null);
        assertEquals(tenantConfig.isValid(), true);
    }

    @Test
    public void isNotValid() throws Exception {
        tenantConfig = new TenantConfiguration(projectKey, clientId, clientSecret, endpointUrl, "", false, 100L);
        assertEquals(tenantConfig.isValid(), false);

        tenantConfig = new TenantConfiguration(projectKey, clientId, clientSecret, "", secretKey, false, 100L);
        assertEquals(tenantConfig.isValid(), false);

        tenantConfig = new TenantConfiguration(projectKey, clientId, "", endpointUrl, secretKey, false, 100L);
        assertEquals(tenantConfig.isValid(), false);

        tenantConfig = new TenantConfiguration(projectKey, "", clientSecret, endpointUrl, secretKey, false, 100L);
        assertEquals(tenantConfig.isValid(), false);

        tenantConfig = new TenantConfiguration("", clientId, clientSecret, endpointUrl, secretKey, false, 100L);
        assertEquals(tenantConfig.isValid(), false);

        tenantConfig = new TenantConfiguration("", "", "", "", "", false, 100L);
        assertEquals(tenantConfig.isValid(), false);
    }

    @Test
    public void getSphereClient() throws Exception {
        final SphereClient client = tenantConfig.getSphereClient();
        final SphereApiConfig clientConfig = client.getConfig();
        assertEquals(clientConfig.getProjectKey(), projectKey);
    }
}