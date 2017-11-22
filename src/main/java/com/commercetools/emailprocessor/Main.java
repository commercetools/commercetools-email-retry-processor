package com.commercetools.emailprocessor;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.commercetools.emailprocessor.jobs.EmailJob.process;
import static com.commercetools.emailprocessor.utils.ConfigurationUtils.getConfiguration;
import static org.apache.commons.lang3.ArrayUtils.isEmpty;


public class Main {
    /**
     * This jobs try to resend emails of a given List of tenant using the EmailProcessor
     * The configuration of this job, can be pass by  a File or enviroment variable.
     *
     * @param args all args
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(final String[] args) {
        getConfiguration(isEmpty(args) ? args[0] : "")
                .ifPresent(config -> process(config)
                        .forEach(statistic -> statistic.print(LOGGER)));

    }
}
