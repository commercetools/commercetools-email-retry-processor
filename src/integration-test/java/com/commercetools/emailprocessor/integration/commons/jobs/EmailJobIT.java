package com.commercetools.emailprocessor.integration.commons.jobs;

import com.commercetools.emailprocessor.email.EmailProcessor;
import com.commercetools.emailprocessor.jobs.EmailJob;
import com.commercetools.emailprocessor.model.ProjectConfiguration;
import com.commercetools.emailprocessor.model.Statistics;
import com.commercetools.emailprocessor.utils.ConfigurationUtils;
import com.fasterxml.jackson.databind.JsonNode;
import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.client.SphereRequest;
import io.sphere.sdk.customobjects.CustomObjectDraft;
import io.sphere.sdk.customobjects.commands.CustomObjectDeleteCommand;
import io.sphere.sdk.customobjects.commands.CustomObjectUpsertCommand;
import io.sphere.sdk.customobjects.queries.CustomObjectQuery;
import io.sphere.sdk.json.SphereJsonUtils;
import io.sphere.sdk.queries.QueryDsl;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.function.Supplier;

import static io.sphere.sdk.queries.QueryExecutionUtils.queryAll;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

public class EmailJobIT {
    private static final Logger LOG = LoggerFactory.getLogger(EmailJobIT.class);

    private static SphereClient ctpClient;
    private static ProjectConfiguration configuration = null;

    /**
     * Load configuration from environment variables.
     */
    @BeforeClass
    public static void setup() {
        configuration = ConfigurationUtils.getConfiguration("").get();
        assertThat(configuration).isNotNull();
        assertThat(configuration.isValid()).isTrue();
        ctpClient = configuration.getTenants().get(0).getSphereClient();

    }

    /**
     * Create required Email objects.
     */
    @Before
    public void setupTest() {
        queryAndApply(ctpClient, CustomObjectQuery::ofJsonNode, CustomObjectDeleteCommand::ofJsonNode);
    }

    /**
     * Cleans up the ctp project.
     */
    @AfterClass
    public static void tearDown() {
        queryAndApply(ctpClient, CustomObjectQuery::ofJsonNode, CustomObjectDeleteCommand::ofJsonNode);
    }



    @Test
    public void process_WithASuccessfulEmail_ShouldReturnNoError() {

        createCustomObject(EmailProcessor.EMAIL_STATUS_PENDING, "1");
        createCustomObject(EmailProcessor.EMAIL_STATUS_PENDING, "2");
        createCustomObject(EmailProcessor.EMAIL_STATUS_ERROR, "3");
        configuration.getTenants().get(0).setApiEndpointURL("https://httpbin.org/status/" + Statistics
                .RESPONSE_CODE_SUCCESS);
        List<Statistics> statistics = EmailJob.process(configuration);
        assertThat(statistics).isNotEmpty();
        Statistics statistic = statistics.get(0);
        assertThat(statistic.getProcessedEmails()).isEqualTo(2);
        assertThat(statistic.getNotProcessedEmails()).isEqualTo(1);
        assertThat(statistic.getSuccessfulSendedEmails()).isEqualTo(2);
        assertThat(statistic.getTemporarilyErrors()).isEqualTo(0);
        assertThat(statistic.getPermanentErrors()).isEqualTo(0);
    }

    @Test
    public void process_WithIncorrectEndpointUrl_ShouldReturnEmptyStatics() {

        createCustomObject(EmailProcessor.EMAIL_STATUS_PENDING, "1");
        createCustomObject(EmailProcessor.EMAIL_STATUS_PENDING, "2");
        createCustomObject(EmailProcessor.EMAIL_STATUS_ERROR, "3");
        configuration.getTenants().get(0).setApiEndpointURL("https://unknownEndpoint.de");
        List<Statistics> statistics = EmailJob.process(configuration);
        assertThat(statistics).isNotEmpty();
        Statistics statistic = statistics.get(0);
        statistic.print(LOG);
        assertThat(statistic.getProcessedEmails()).isEqualTo(0);
        assertThat(statistic.getNotProcessedEmails()).isEqualTo(0);
        assertThat(statistic.getSuccessfulSendedEmails()).isEqualTo(0);
        assertThat(statistic.getTemporarilyErrors()).isEqualTo(0);
        assertThat(statistic.getPermanentErrors()).isEqualTo(0);
    }

    @Test
    public void process_WithUnsuccessfulEmail_ShouldReturnPermenantError() {
        createCustomObject(EmailProcessor.EMAIL_STATUS_PENDING, "1");
        createCustomObject(EmailProcessor.EMAIL_STATUS_PENDING, "2");
        createCustomObject(EmailProcessor.EMAIL_STATUS_ERROR, "3");
        configuration.getTenants().get(0).setApiEndpointURL("https://httpbin.org/status/" + Statistics
                .RESPONSE_ERROR_PERMANENT);
        List<Statistics> statistics = EmailJob.process(configuration);
        assertThat(statistics).isNotEmpty();
        Statistics statistic = statistics.get(0);
        assertThat(statistic.getProcessedEmails()).isEqualTo(2);
        assertThat(statistic.getNotProcessedEmails()).isEqualTo(1);
        assertThat(statistic.getSuccessfulSendedEmails()).isEqualTo(0);
        assertThat(statistic.getTemporarilyErrors()).isEqualTo(0);
        assertThat(statistic.getPermanentErrors()).isEqualTo(2);
    }

    @Test
    public void process_WithUnsuccessfulEmail_ShouldReturnTemporaryError() {
        createCustomObject(EmailProcessor.EMAIL_STATUS_PENDING, "1");
        createCustomObject(EmailProcessor.EMAIL_STATUS_PENDING, "2");
        createCustomObject(EmailProcessor.EMAIL_STATUS_ERROR, "3");
        configuration.getTenants().get(0).setApiEndpointURL("https://httpbin.org/status/" + Statistics
                .RESPONSE_ERROR_TEMP);
        List<Statistics> statistics = EmailJob.process(configuration);
        assertThat(statistics).isNotEmpty();
        Statistics statistic = statistics.get(0);
        assertThat(statistic.getProcessedEmails()).isEqualTo(2);
        assertThat(statistic.getNotProcessedEmails()).isEqualTo(1);
        assertThat(statistic.getSuccessfulSendedEmails()).isEqualTo(0);
        assertThat(statistic.getTemporarilyErrors()).isEqualTo(2);
        assertThat(statistic.getPermanentErrors()).isEqualTo(0);
    }


    /**
     * Applies the {@code pageMapper} function on each page fetched from the supplied {@code
     * queryRequestSupplier} on
     * the supplied {@code ctpClient}.
     *
     * @param ctpClient            defines the CTP project to apply the query on.
     * @param queryRequestSupplier defines a supplier which, when executed, returns the query that should be made on
     *                             the CTP project.
     * @param resourceMapper       defines a mapper function that should be applied on each resource in the
     *                             fetched page
     *                             from the query on the specified CTP project.
     */


    public static <T, C extends QueryDsl<T, C>> void queryAndApply(
            @Nonnull final SphereClient ctpClient,
            @Nonnull final Supplier<QueryDsl<T, C>> queryRequestSupplier,
            @Nonnull final Function<T, SphereRequest<T>> resourceMapper) {
        queryAll(ctpClient, queryRequestSupplier.get(), resourceMapper)
                .thenApply(allRequests -> allRequests.stream()
                        .map(ctpClient::execute)
                        .map(CompletionStage::toCompletableFuture).collect(toList()))
                .thenApply(list -> list.toArray(new CompletableFuture[list.size()]))
                .thenCompose(CompletableFuture::allOf)
                .toCompletableFuture().join();
    }

    private void createCustomObject(final String status,final String errorMailId) {
        JsonNode jsonNode = SphereJsonUtils.parse(String.format("{\"status\":\"%s\"}", status));
        CustomObjectDraft<JsonNode> draft = CustomObjectDraft.ofUnversionedUpsert(EmailProcessor.CONTAINER_ID,
                errorMailId, jsonNode);
        ctpClient.execute(CustomObjectUpsertCommand.of(draft)).toCompletableFuture().join();

    }
}
