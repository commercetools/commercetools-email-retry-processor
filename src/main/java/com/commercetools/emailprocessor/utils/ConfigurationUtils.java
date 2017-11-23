package com.commercetools.emailprocessor.utils;


import com.commercetools.emailprocessor.model.ProjectConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Optional;


public class ConfigurationUtils {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationUtils.class);
    public static final String CTP_PROJECT_CONFIG = "CTP_PROJECT_CONFIG";

    /**
     * Load a configuration from a enviroment variable or, if given, a configuration file.
     *
     * @param resourcePath optional path to a configuration file
     * @return a projectconfiguration
     */
    public static Optional<ProjectConfiguration> getConfiguration(final String resourcePath) {
        ObjectMapper objectMapper = new ObjectMapper();
        ProjectConfiguration projectConfiguration = null;
        try {
            if (StringUtils.isNotEmpty(resourcePath)) {
                File file = new File(resourcePath);
                try {
                    projectConfiguration = objectMapper.readValue(file, ProjectConfiguration.class);
                } catch (IOException exception) {
                    LOG.error("The file cannot be parsed", exception);
                }
            } else {
                String ctpProjectConfig = System.getenv(CTP_PROJECT_CONFIG);
                if (StringUtils.isNotEmpty(ctpProjectConfig)) {
                    projectConfiguration = objectMapper.readValue(ctpProjectConfig, ProjectConfiguration.class);
                }
            }
        } catch (IOException jsonExecption) {
            LOG.error("The json stream cannot be parsed", jsonExecption);
        }

        if (projectConfiguration == null) {
            LOG.error("The projectconfiguration cannot be loaded");
        }
        return Optional.ofNullable(projectConfiguration);
    }
}
