package com.commercetools.emailprocessor.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectConfiguration {
    private static final Logger LOG = LoggerFactory.getLogger(ProjectConfiguration.class);
    private List<TenantConfiguration> tenants;

    public ProjectConfiguration() {

    }

    public ProjectConfiguration(final List<TenantConfiguration> currentTenants) {
        this.tenants = currentTenants;
    }

    public List<TenantConfiguration> getTenants() {
        return tenants;
    }

    /**
     * Validates the current project configuration.
     *
     * @return true, if the configuration is valid
     */
    public boolean isValid() {
        return tenants != null && tenants.size() > 0 && tenants.stream().filter(t -> !t.isValid()).count() == 0;
    }


}
