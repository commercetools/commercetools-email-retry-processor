package com.commercetools.emailprocessor.jobs;


import com.commercetools.emailprocessor.email.EmailProcessor;
import com.commercetools.emailprocessor.model.ProjectConfiguration;
import com.commercetools.emailprocessor.model.Statistics;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.allOf;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.stream.Collectors.toList;


public class EmailJob {

    private static final Logger LOG = LoggerFactory.getLogger(EmailJob.class);

    /**
     * This jobs try to resend emails of a given List of tenant using the EmailProcessor
     * The configuration of this job, can be pass by  a File or enviroment variable.
     *
     * @param projectConfiguration configuration of a given Project
     */


    public static List<Statistics> process(final ProjectConfiguration projectConfiguration) {
        if (projectConfiguration != null && projectConfiguration.isValid()) {
            EmailProcessor emailProcessor = new EmailProcessor();

            final List<CompletionStage<Statistics>> listOfStageOfStatistics = projectConfiguration.getTenants()
                    .parallelStream()
                    .map(tenantConfiguration -> {
                                try {
                                    return emailProcessor.processEmails(tenantConfiguration);
                                } catch (Exception e) {
                                    LOG.error("Error in email processing for tenant [{}].",
                                            tenantConfiguration.getProjectKey(), e);
                                }
                                return completedFuture(new Statistics());
                            }
                    )
                    .collect(toList());

            final List<Statistics> listOfStatistics = allOf(listOfStageOfStatistics.toArray(new CompletableFuture[0]))
                    .thenApply(ignoreVoid -> listOfStageOfStatistics.stream()
                            .map(CompletionStage::toCompletableFuture)
                            .map(CompletableFuture::join)
                            .collect(toList()))
                    .toCompletableFuture().join();

            return listOfStatistics;
        } else {
            LOG.error("NO valid config was found");

        }
        return Collections.emptyList();
    }
}
