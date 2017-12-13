package com.commercetools.emailprocessor.utils;


import com.commercetools.emailprocessor.model.ProjectConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

import static com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_SINGLE_QUOTES;
import static org.apache.commons.lang3.StringUtils.isBlank;


public class ConfigurationUtils {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationUtils.class);

    /**
     * Load a configuration from  a configuration file.
     *
     * @param resourcePath optional path to a configuration file
     * @return a projectconfiguration
     */
    public static Optional<ProjectConfiguration> getConfigurationFromFile(@Nonnull final String resourcePath) {
        Optional<ProjectConfiguration> projectConfiguration = Optional.empty();
        try {
            projectConfiguration = getConfigurationFromString(new String(Files.readAllBytes(Paths.get(resourcePath)), Charset
                .defaultCharset()));
        } catch (IOException exception) {
            LOG.error(String.format("The File '%s' cannot be parsed", resourcePath), exception);
        }
        return projectConfiguration;
    }

    /**
     * Parses a project configuration and validates it.
     *
     * @param ctpProjectConfig String representation of a project configuration
     * @return A Project configuration
     * @throws IOException When the configuration string is not parsable.
     */
    public static Optional<ProjectConfiguration> getConfigurationFromString(final String ctpProjectConfig)  {
        ProjectConfiguration projectConfiguration = null;
        if (StringUtils.isNotBlank(ctpProjectConfig)) {
            final ObjectMapper objectMapper = new ObjectMapper();
            //Allows single Quotes within the json files
            objectMapper.enable(ALLOW_SINGLE_QUOTES);
            try {
                projectConfiguration = objectMapper.readValue(ctpProjectConfig, ProjectConfiguration.class);

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
            } catch (IOException exception) {
                LOG.error("Cannot parse configuration",exception);
            }
        }
        return Optional.of(projectConfiguration);
    }
}