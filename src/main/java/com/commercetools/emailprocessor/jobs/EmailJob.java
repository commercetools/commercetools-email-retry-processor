package com.commercetools.emailprocessor.jobs;

import com.commercetools.emailprocessor.model.ProjectConfiguration;
import com.commercetools.emailprocessor.model.Statistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static com.commercetools.emailprocessor.email.EmailProcessor.of;
import static java.util.concurrent.CompletableFuture.allOf;
import static java.util.stream.Collectors.toList;


public class EmailJob {

    private static final Logger LOG = LoggerFactory.getLogger(EmailJob.class);

    /**
     * This job tries to process emails of a given List of tenant using the EmailProcessor
     * The configuration of this job, can be passed by  a file or environment variable.
     *
     * @param projectConfiguration configuration of a given Project
     */
    public static CompletionStage<List<Statistics>> process(@Nonnull final ProjectConfiguration projectConfiguration) {


        final List<CompletableFuture<Statistics>> listOfStageOfStatistics = projectConfiguration.getTenants()
                .parallelStream()
                .map(tenantConfiguration -> {
                            try {
                                return of().processEmails(tenantConfiguration).toCompletableFuture();
                            } catch (Exception exception) {
                                LOG.error(String.format("Error in email processing for tenant %s.",
                                        tenantConfiguration.getProjectKey()), exception);
                            }
                            MDC.clear();
                            return CompletableFuture.completedFuture(Statistics.ofError(tenantConfiguration
                                    .getProjectKey()));
                        }
                )
                .collect(toList());
        return allOf(listOfStageOfStatistics.toArray(new CompletableFuture[listOfStageOfStatistics.size()]))
                .thenApply(ignoreVoid -> listOfStageOfStatistics.stream()
                        .map(CompletableFuture::join)
                        .collect(toList()));
    }
}