package com.commercetools.emailprocessor.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectConfiguration {
    private static final Logger LOG = LoggerFactory.getLogger(ProjectConfiguration.class);

    public ProjectConfiguration() {
    }

    private List<TenantConfiguration> tenants;

    public List<TenantConfiguration> getTenants() {
        return tenants;
    }

    public void setTenants(List<TenantConfiguration> tenants) {
        this.tenants = tenants;
    }

    public boolean isValid() {

        boolean valid = true;
        if (tenants != null && !tenants.isEmpty()) {
            for (TenantConfiguration tenant : tenants) {
                if (!tenant.isValid()) {
                    valid = false;
                }
            }
        } else {
            LOG.error("Please define at least on tenant");
            valid = false;
        }


        return valid;
    }


}
