package com.commercetools.emailprocessor;


import com.commercetools.emailprocessor.email.EmailProcessor;
import com.commercetools.emailprocessor.jobs.EmailJob;
import com.commercetools.emailprocessor.model.ProjectConfiguration;
import com.commercetools.emailprocessor.model.Statistics;
import com.commercetools.emailprocessor.model.TenantConfiguration;
import com.commercetools.emailprocessor.utils.ConfigurationUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.sphere.sdk.json.SphereJsonUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Stream;

import static java.util.concurrent.CompletableFuture.allOf;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.stream.Collectors.toList;


public class Main {
    /**
     * This jobs try to resend emails of a given List of tenant using the EmailProcessor
     * The configuration of this job, can be pass by  a File or enviroment variable.
     *
     * @param args all args
     */
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(final String[] args) {
        String resourcePath = !ArrayUtils.isEmpty(args) ? args[0] : "";
        ProjectConfiguration projectConfiguration = ConfigurationUtils.getConfiguration(resourcePath);
        if (projectConfiguration != null) {
            EmailJob.process(projectConfiguration).stream().map(statistic -> {
                        statistic.print();
                        return "";
                    }
            );
        }

    }
}
