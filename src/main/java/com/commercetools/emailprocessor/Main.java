package com.commercetools.emailprocessor;


import com.commercetools.emailprocessor.model.ProjectConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.commercetools.emailprocessor.jobs.EmailJob.process;
import static com.commercetools.emailprocessor.utils.ConfigurationUtils.getConfigurationFromFile;
import static com.commercetools.emailprocessor.utils.ConfigurationUtils.getConfigurationFromString;
import static java.lang.String.format;
import static org.apache.commons.lang3.ArrayUtils.isEmpty;


public class Main {

    public static final String CTP_PROJECT_CONFIG = "CTP_PROJECT_CONFIG";
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    private static final String PROJECT_NAME = "commercetools-email-retry-processor";

    /**
     * Triggers the execution of the email job.
     *
     * @param args optional path to a configuration file.
     */
    public static void main(final String[] args) {
        int exitStatus = 1;
        long startTime = System.nanoTime();

        try {
            LOGGER.info(format("%s started", PROJECT_NAME));
            final Optional<ProjectConfiguration> configurationOpt = !isEmpty(args) ? getConfigurationFromFile(args[0])
                    : getConfigurationFromString(System.getenv(CTP_PROJECT_CONFIG));

            exitStatus = configurationOpt.map(config -> {
                process(config)
                        .thenAccept(statistics -> statistics.forEach(statistic -> statistic.print(LOGGER)))
                        .toCompletableFuture().join();

                final long elapsedTime = System.nanoTime() - startTime;
                final double seconds = TimeUnit.SECONDS.convert(elapsedTime, TimeUnit.NANOSECONDS);
                LOGGER.info(format("%s completed after %s second", PROJECT_NAME, seconds));
                return 0;
            }).orElseGet(() -> {
                LOGGER.error(format("%s failed: The project configuration cannot be loaded", PROJECT_NAME));
                return 1;
            });
        } catch (Throwable throwable) {
            LOGGER.error(format("%s failed", PROJECT_NAME), throwable);
        } finally {
            System.exit(exitStatus);
        }
    }
}
