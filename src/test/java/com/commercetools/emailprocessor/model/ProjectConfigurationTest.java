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
        ProjectConfiguration configuration = new ProjectConfiguration(Collections.singletonList(new TenantConfiguration(
            projectKey, clientId, clientSecret, endPointUrl)));
        assertEquals(configuration.isValid(), true);


        List<TenantConfiguration> tenantList = new ArrayList<TenantConfiguration>();
        tenantList.add(0, new TenantConfiguration(projectKey, clientId,
            clientSecret, endPointUrl));
        tenantList.add(1, new TenantConfiguration(projectKey, clientId,
            clientSecret, endPointUrl));

        configuration = new ProjectConfiguration(tenantList);
        assertEquals(configuration.isValid(), true);

    }

    @Test
    public void configurationShouldNotBeValid() throws Exception {
        ProjectConfiguration configuration = new ProjectConfiguration(null);
        assertEquals(configuration.isValid(), false);

        configuration = new ProjectConfiguration(Collections.emptyList());

        assertEquals(false, configuration.isValid());

        configuration = new ProjectConfiguration(Collections.singletonList(new TenantConfiguration(projectKey, clientId,
            clientSecret, null)));
        assertEquals(configuration.isValid(), false);

        configuration = new ProjectConfiguration(Collections.singletonList(new TenantConfiguration(projectKey, clientId,
            null, endPointUrl)));
        assertEquals(configuration.isValid(), false);

        configuration = new ProjectConfiguration(Collections.singletonList(new TenantConfiguration(projectKey, null,
            clientSecret, endPointUrl)));
        assertEquals(configuration.isValid(), false);

        configuration = new ProjectConfiguration(Collections.singletonList(new TenantConfiguration("", clientId,
            clientSecret, endPointUrl)));
        assertEquals(configuration.isValid(), false);

        configuration = new ProjectConfiguration(Collections.singletonList(new TenantConfiguration(projectKey, clientId,
            clientSecret, null)));
        assertEquals(configuration.isValid(), false);


        List<TenantConfiguration> tenantList = new ArrayList<TenantConfiguration>();
        tenantList.add(0, new TenantConfiguration(projectKey, clientId,
            clientSecret, null));
        tenantList.add(1, new TenantConfiguration(projectKey, clientId,
            clientSecret, null));
        configuration = new ProjectConfiguration(tenantList);
        assertEquals(configuration.isValid(), false);

    }


}