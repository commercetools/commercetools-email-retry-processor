package com.commercetools.emailprocessor.utils;


import com.commercetools.emailprocessor.jobs.EmailJob;
import com.commercetools.emailprocessor.model.ProjectConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;


public class ConfigurationUtils {
    /**
     * This jobs try to resend emails of a given List of tenant using the EmailProcessor
     * The configuration of this job, can be pass by  a File or enviroment variable.
     *
     * @param args all args
     */
    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationUtils.class);
    public static String CTP_PROJECT_CONFIG = "CTP_PROJECT_CONFIG";

    public static ProjectConfiguration getConfiguration (final  String resourcePath ) {
        ObjectMapper objectMapper = new ObjectMapper();
        ProjectConfiguration projectConfiguration = null;
        try {

            if (StringUtils.isNotEmpty(resourcePath)) {
                File file = new File(resourcePath);
                 try {
                    projectConfiguration = objectMapper.readValue(file, ProjectConfiguration.class);
                } catch (IOException e) {
                    LOG.error("The File cannot be parsed", e);
                }

            } else {
                String ctpProjectConfig = System.getenv(CTP_PROJECT_CONFIG);
                if (StringUtils.isNotEmpty(ctpProjectConfig)) {
                    projectConfiguration = objectMapper.readValue(ctpProjectConfig, ProjectConfiguration.class);
                }
            }
        } catch (IOException e) {
            LOG.error("The json stream cannot be parsed", e);
        }


   return projectConfiguration;
    }
}
