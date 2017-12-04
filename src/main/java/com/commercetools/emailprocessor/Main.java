package com.commercetools.emailprocessor;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.commercetools.emailprocessor.jobs.EmailJob.process;
import static com.commercetools.emailprocessor.utils.ConfigurationUtils.getConfiguration;
import static org.apache.commons.lang3.ArrayUtils.isEmpty;


public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    /**
     * Triggers the execution of the email job.
     *
     * @param args optional path to a configuration file.
     */
    public static void main(final String[] args) {
        getConfiguration(!isEmpty(args) ? args[0] : "")
            .ifPresent(config -> process(config)
                .forEach(statistic -> statistic.print(LOGGER)));
    }
}
