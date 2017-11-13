package com.commercetools.emailprocessor;


import com.commercetools.emailprocessor.model.ProjectConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.sphere.sdk.json.SphereJsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;


public class Main {
    /**
     * Application entry point.
     *
     * @param args all args
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    public static String CTP_PROJECT_CONFIG = "git";

    public static void main(final String[] args) {
        ObjectMapper objectMapper = new ObjectMapper();
        ProjectConfiguration config = null;
        try {

        if (args != null && args.length > 0) {
            String resourcePath = args[0];
            File file = new File(resourcePath);

            try {
                config = objectMapper.readValue(file, ProjectConfiguration.class);
            } catch (IOException e) {
                LOGGER.error("The File cannot be parsed", e);
            }

        } else {
            final String ctpProjectConfig = System.getenv(CTP_PROJECT_CONFIG);
            if (StringUtils.isNotEmpty(ctpProjectConfig)) {

                config =objectMapper.readValue(ctpProjectConfig, ProjectConfiguration.class);
            }
        }
    } catch (IOException e) {
        LOGGER.error("The File cannot be parsed", e);
    }
        if (config != null && config.isValid()) {
            LOGGER.error("A valid config was found");
        } else {
            LOGGER.error("NO valid config was found");

        }

    }
}
