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

    @Test
    public void configurationShouldBeValid() throws Exception {
        ProjectConfiguration configuration = new ProjectConfiguration();
        configuration.setTenants(Collections.singletonList(new TenantConfiguration(projectKey, clientId,
            clientSecret, endPointUrl)));
        assertEquals(configuration.isValid(), true);

        configuration = new ProjectConfiguration();
        List<TenantConfiguration> tenantList = new ArrayList<TenantConfiguration>();
        tenantList.add(0, new TenantConfiguration(projectKey, clientId,
            clientSecret, endPointUrl));
        tenantList.add(1, new TenantConfiguration(projectKey, clientId,
            clientSecret, endPointUrl));

        configuration.setTenants(tenantList);
        assertEquals(configuration.isValid(), true);

    }

    @Test
    public void configurationShouldNotBeValid() throws Exception {
        ProjectConfiguration configuration = new ProjectConfiguration();
        assertEquals(configuration.isValid(), false);

        configuration = new ProjectConfiguration();
        configuration.setTenants(Collections.emptyList());
        assertEquals(false, configuration.isValid());

        configuration = new ProjectConfiguration();
        configuration.setTenants(Collections.singletonList(new TenantConfiguration(projectKey, clientId,
            clientSecret, null)));
        assertEquals(configuration.isValid(), false);

        configuration = new ProjectConfiguration();
        configuration.setTenants(Collections.singletonList(new TenantConfiguration(projectKey, clientId,
            null, endPointUrl)));
        assertEquals(configuration.isValid(), false);

        configuration = new ProjectConfiguration();
        configuration.setTenants(Collections.singletonList(new TenantConfiguration(projectKey, null,
            clientSecret, endPointUrl)));
        assertEquals(configuration.isValid(), false);

        configuration = new ProjectConfiguration();
        configuration.setTenants(Collections.singletonList(new TenantConfiguration("", clientId,
            clientSecret, endPointUrl)));
        assertEquals(configuration.isValid(), false);

        configuration = new ProjectConfiguration();
        configuration.setTenants(Collections.singletonList(new TenantConfiguration(projectKey, clientId,
            clientSecret, null)));
        assertEquals(configuration.isValid(), false);

        configuration = new ProjectConfiguration();
        List<TenantConfiguration> tenantList = new ArrayList<TenantConfiguration>();
        tenantList.add(0, new TenantConfiguration(projectKey, clientId,
            clientSecret, null));
        tenantList.add(1, new TenantConfiguration(projectKey, clientId,
            clientSecret, endPointUrl));

        configuration.setTenants(tenantList);
        assertEquals(configuration.isValid(), false);

    }


}