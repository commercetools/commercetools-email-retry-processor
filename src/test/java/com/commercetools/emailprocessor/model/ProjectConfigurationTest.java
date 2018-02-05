package com.commercetools.emailprocessor.model;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ProjectConfigurationTest {

    private String projectKey = "anyKey";
    private String clientId = "anyID";
    private String clientSecret = "anySecret";
    private String endPointUrl = "anyUrl";
    private String encryptionKey = "encryptionKey";

    @Test
    public void configurationShouldBeValid() throws Exception {
        ProjectConfiguration configuration = new ProjectConfiguration(Collections.singletonList(
            new TenantConfiguration(projectKey, clientId, clientSecret, endPointUrl, encryptionKey, true,100L)));
        assertEquals(configuration.isValid(), true);

        List<TenantConfiguration> tenantList = new ArrayList<TenantConfiguration>();
        tenantList.add(0, new TenantConfiguration(projectKey, clientId,
            clientSecret, endPointUrl, encryptionKey, true,100L));
        tenantList.add(1, new TenantConfiguration(projectKey, clientId,
            clientSecret, endPointUrl, encryptionKey, false,100L));
        tenantList.add(1, new TenantConfiguration(projectKey, clientId,
                clientSecret, endPointUrl, encryptionKey, false,null));
        configuration = new ProjectConfiguration(tenantList);
        assertEquals(configuration.isValid(), true);
    }

    @Test
    public void configurationShouldNotBeValid() throws Exception {
        ProjectConfiguration configuration = new ProjectConfiguration(Collections.emptyList());
        assertEquals(false, configuration.isValid());

        configuration = new ProjectConfiguration(Collections.singletonList(new TenantConfiguration(projectKey, clientId,
            clientSecret, "", encryptionKey,  true,100L)));
        assertEquals(configuration.isValid(), false);

        configuration = new ProjectConfiguration(Collections.singletonList(new TenantConfiguration(projectKey, clientId,
            "", endPointUrl, encryptionKey, true ,100L)));
        assertEquals(configuration.isValid(), false);

        configuration = new ProjectConfiguration(Collections.singletonList(new TenantConfiguration(projectKey, "",
            clientSecret, endPointUrl, encryptionKey, true,100L)));
        assertEquals(configuration.isValid(), false);

        configuration = new ProjectConfiguration(Collections.singletonList(new TenantConfiguration("", clientId,
            clientSecret, endPointUrl, encryptionKey, true,100L)));
        assertEquals(configuration.isValid(), false);

        List<TenantConfiguration> tenantList = new ArrayList<TenantConfiguration>();
        tenantList.add(0, new TenantConfiguration(projectKey, clientId,
            clientSecret, "", encryptionKey, false,100L));
        tenantList.add(1, new TenantConfiguration(projectKey, clientId,
            clientSecret, endPointUrl, "", false,100L));
        configuration = new ProjectConfiguration(tenantList);
        assertEquals(configuration.isValid(), false);
    }
}