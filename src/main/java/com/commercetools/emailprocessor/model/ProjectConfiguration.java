package com.commercetools.emailprocessor.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.annotation.Nonnull;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class ProjectConfiguration {

    private List<TenantConfiguration> tenants;

    public ProjectConfiguration() {
    }

    public ProjectConfiguration(@Nonnull final List<TenantConfiguration> currentTenants) {
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
        return tenants != null && !tenants.isEmpty() && tenants.stream().filter(t -> !t.isValid()).count() == 0;
    }
}
