package com.commercetools.emailprocessor;


import com.commercetools.emailprocessor.model.ProjectConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static com.commercetools.emailprocessor.jobs.EmailJob.process;
import static com.commercetools.emailprocessor.utils.ConfigurationUtils.getConfigurationFromFile;
import static com.commercetools.emailprocessor.utils.ConfigurationUtils.getConfigurationFromString;
import static org.apache.commons.lang3.ArrayUtils.isEmpty;


public class Main {

    public static final String CTP_PROJECT_CONFIG = "CTP_PROJECT_CONFIG";
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    /**
     * Triggers the execution of the email job.
     *
     * @param args optional path to a configuration file.
     */
    public static void main(final String[] args) {
        final Optional<ProjectConfiguration> configurationOpt = !isEmpty(args) ? getConfigurationFromFile(args[0])
            : getConfigurationFromString(System.getenv(CTP_PROJECT_CONFIG));
        final Integer exitStatus = configurationOpt.map(config -> {
            process(config)
                .thenAccept(statistics -> statistics.forEach(statistic -> statistic.print(LOGGER)))
                .toCompletableFuture().join();
            return 0;
        }).orElseGet(() -> {
            LOGGER.error("The project configuration cannot be loaded");
            return 1;
        });

        System.exit(exitStatus);
    }
}