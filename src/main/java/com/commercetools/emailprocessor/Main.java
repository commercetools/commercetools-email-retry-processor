package com.commercetools.emailprocessor;


import com.commercetools.emailprocessor.model.ProjectConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

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
        bridgeJULToSLF4J();

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

    /**
     * Routes all incoming j.u.l. (java.util.logging.Logger) records to the SLF4j API. This is done by:
     * <ol>
     *     <li>Removing existing handlers attached to the j.u.l root logger.</li>
     *     <li>Adding SLF4JBridgeHandler to j.u.l's root logger.</li>
     * </ol>
     * <p>Why we do the routing?
     * <p>Some dependencies (e.g. org.javamoney.moneta's DefaultMonetaryContextFactory) log events using the
     * j.u.l. This causes such logs to ignore the logback.xml configuration which is only
     * applied to logs from the SLF4j implementation.
     *
     */
    private static void bridgeJULToSLF4J() {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }
}
