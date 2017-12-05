package com.commercetools.emailprocessor.utils;


import com.commercetools.emailprocessor.model.ProjectConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

import static com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_SINGLE_QUOTES;
import static org.apache.commons.lang3.StringUtils.isBlank;


public class ConfigurationUtils {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationUtils.class);
    private static final String CTP_PROJECT_CONFIG = "CTP_PROJECT_CONFIG";

    /**
     * Load a configuration from a environment variable..
     *
     * @return a projectconfiguration
     */
    public static Optional<ProjectConfiguration> getConfigurationFromEnvVar() {
        final ObjectMapper objectMapper = new ObjectMapper();
        //Allows single Quotes within the json files
        objectMapper.enable(ALLOW_SINGLE_QUOTES);
        ProjectConfiguration projectConfiguration = null;
        try {
            final String ctpProjectConfig = System.getenv(CTP_PROJECT_CONFIG);
            if (StringUtils.isNotBlank(ctpProjectConfig)) {
                projectConfiguration = objectMapper.readValue(ctpProjectConfig, ProjectConfiguration.class);
            }
        } catch (IOException exception) {
            LOG.error("The file cannot be parsed", exception);
        }
        return getConfiguration(projectConfiguration);
    }


    /**
     * Load a configuration from  a configuration file.
     *
     * @param resourcePath optional path to a configuration file
     * @return a projectconfiguration
     */
    public static Optional<ProjectConfiguration> getConfigurationFromFile(@Nonnull final String resourcePath) {
        final ObjectMapper objectMapper = new ObjectMapper();
        //Allows single Quotes within the json files
        objectMapper.enable(ALLOW_SINGLE_QUOTES);
        ProjectConfiguration projectConfiguration = null;
        try {
            final File file = new File(resourcePath);
            projectConfiguration = objectMapper.readValue(file, ProjectConfiguration.class);
        } catch (IOException exception) {
            LOG.error("The file cannot be parsed", exception);
        }
        return getConfiguration(projectConfiguration);
    }


    private static Optional<ProjectConfiguration> getConfiguration(final ProjectConfiguration projectConfiguration) {
        if (projectConfiguration == null || !projectConfiguration.isValid()) {
            if (projectConfiguration != null) {

                projectConfiguration.getTenants().stream().forEach(tenantConfiguration -> {
                    final String errorMessage = "[" + tenantConfiguration.getProjectKey() + "] "
                        + "Please define the missing Property '%s'";
                    if (isBlank(tenantConfiguration.getProjectKey())) {
                        LOG.error(String.format(errorMessage, "projectKey"));
                    }
                    if (isBlank(tenantConfiguration.getClientId())) {
                        LOG.error(String.format(errorMessage, "clientId"));
                    }
                    if (isBlank(tenantConfiguration.getClientSecret())) {
                        LOG.error(String.format(errorMessage, "clientSecret"));
                    }
                    if (isBlank(tenantConfiguration.getEndpointUrl())) {
                        LOG.error(String.format(errorMessage, "endpointUrl"));
                    }
                    if (isBlank(tenantConfiguration.getEncryptionKey())) {
                        LOG.error(String.format(errorMessage, "encryptionKey"));
                    }
                });
            }
            return Optional.empty();

        }

        return Optional.of(projectConfiguration);
    }

}