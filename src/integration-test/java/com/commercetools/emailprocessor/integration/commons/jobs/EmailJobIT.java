package com.commercetools.emailprocessor.integration.commons.jobs;

import com.commercetools.emailprocessor.Main;
import com.commercetools.emailprocessor.email.EmailProcessor;
import com.commercetools.emailprocessor.jobs.EmailJob;
import com.commercetools.emailprocessor.model.ProjectConfiguration;
import com.commercetools.emailprocessor.model.Statistics;
import com.commercetools.emailprocessor.model.TenantConfiguration;
import com.commercetools.emailprocessor.testutils.TestUtils;
import com.fasterxml.jackson.databind.JsonNode;
import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.client.SphereRequest;
import io.sphere.sdk.customobjects.CustomObject;
import io.sphere.sdk.customobjects.CustomObjectDraft;
import io.sphere.sdk.customobjects.commands.CustomObjectDeleteCommand;
import io.sphere.sdk.customobjects.commands.CustomObjectUpsertCommand;
import io.sphere.sdk.customobjects.queries.CustomObjectQuery;
import io.sphere.sdk.json.SphereJsonUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.commercetools.emailprocessor.Main.CTP_PROJECT_CONFIG;
import static com.commercetools.emailprocessor.email.EmailProcessor.EMAIL_STATUS_ERROR;
import static com.commercetools.emailprocessor.email.EmailProcessor.STATUS_PENDING;
import static com.commercetools.emailprocessor.utils.ConfigurationUtils.getConfigurationFromString;
import static io.sphere.sdk.queries.QueryExecutionUtils.queryAll;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class EmailJobIT {
    private static final Logger LOG = LoggerFactory.getLogger(EmailJobIT.class);
    private static final String HTTPBIN_DOMAIN = "https://httpbin.org/status/";
    @Rule
    public final ExpectedSystemExit exitRule = ExpectedSystemExit.none();
    private SphereClient ctpClient;
    private ProjectConfiguration configuration = null;

    /**
     * Load configuration from environment variables and cleanup project.
     */
    @Before
    public void setup() {
        getConfigurationFromString(System.getenv(CTP_PROJECT_CONFIG)).ifPresent(config -> {
            configuration = config;
        });
        ctpClient = configuration.getTenants().get(0).getSphereClient();
        queryAndApply(ctpClient, CustomObjectQuery::ofJsonNode, CustomObjectDeleteCommand::ofJsonNode);
    }

    @Test
    public void getConfiguration_passValidConfigurations_shouldReturnValid() {
        assertEquals("The Configuration should not be null", true, configuration != null);
        assertThat(configuration).isNotNull();
        assertThat(configuration.isValid()).isTrue();
    }

    @Test
    public void main_passValidConfigurations_shouldReturnCorrectExitCode() {
        exitRule.expectSystemExitWithStatus(0);
        Main.main(new String[0]);
    }

    @Test
    public void main_passInValidValidConfigurations_shouldReturnCorrectExitCode() {
        exitRule.expectSystemExitWithStatus(1);
        String[] args = new String[1];
        args[0] = TestUtils.getInvalidProjectConfigurationFilePath();
        Main.main(args);
    }

    @Test
    public void process_withASuccessfulEmail_shouldReturnNoError() {
        createCustomObject(STATUS_PENDING, "1");
        createCustomObject(STATUS_PENDING, "2");
        createCustomObject(EMAIL_STATUS_ERROR, "3");
        configuration.getTenants().get(0).setEndpointUrl(HTTPBIN_DOMAIN + Statistics.RESPONSE_CODE_SUCCESS);
        List<Statistics> statistics = EmailJob.process(configuration).toCompletableFuture().join();
        assertThat(statistics).isNotEmpty();
        Statistics statistic = statistics.get(0);
        assertThat(statistic.getProcessed()).isEqualTo(2);
        assertThat(statistic.getSentSuccessfully()).isEqualTo(2);
        assertThat(statistic.getTemporaryErrors()).isEqualTo(0);
        assertThat(statistic.getPermanentErrors()).isEqualTo(0);
    }

    @Test
    public void process_withManyEmailsObjects_shouldReturnNoError() {
        int numberofEmails = 100;
        int customObjectId = 0;
        while (customObjectId <= numberofEmails) {
            createCustomObject(STATUS_PENDING, String.valueOf(customObjectId));
            customObjectId = customObjectId + 1;
        }
        configuration.getTenants().get(0).setEndpointUrl(HTTPBIN_DOMAIN + Statistics.RESPONSE_CODE_SUCCESS);
        List<Statistics> statistics = EmailJob.process(configuration).toCompletableFuture().join();
        assertThat(statistics).isNotEmpty();
        Statistics statistic = statistics.get(0);
        assertThat(statistic.getProcessed()).isEqualTo(numberofEmails + 1);
        assertThat(statistic.getSentSuccessfully()).isEqualTo(numberofEmails + 1);
        assertThat(statistic.getTemporaryErrors()).isEqualTo(0);
        assertThat(statistic.getPermanentErrors()).isEqualTo(0);
    }

    @Test
    public void process_withOnlyPendingEmailObjectsAndProcessAllIsFalse_shouldProcessAll() {
        configuration.getTenants().get(0).setProcessAll(false);
        createCustomObject(STATUS_PENDING, "1");
        createCustomObject(STATUS_PENDING, "2");
        ;
        configuration.getTenants().get(0).setEndpointUrl(HTTPBIN_DOMAIN + Statistics.RESPONSE_CODE_SUCCESS);

        List<Statistics> statistics = EmailJob.process(configuration).toCompletableFuture().join();
        assertThat(statistics).isNotEmpty();
        Statistics statistic = statistics.get(0);
        assertThat(statistic.getProcessed()).isEqualTo(2);
        assertThat(statistic.getSentSuccessfully()).isEqualTo(2);
        assertThat(statistic.getTemporaryErrors()).isEqualTo(0);
        assertThat(statistic.getPermanentErrors()).isEqualTo(0);
    }

    @Test
    public void process_withOnlyPendingEmailObjectsAndProcessAllIsTrue_shouldProcessAll() {
        configuration.getTenants().get(0).setProcessAll(true);
        createCustomObject(STATUS_PENDING, "1");
        createCustomObject(STATUS_PENDING, "2");
        configuration.getTenants().get(0).setEndpointUrl(HTTPBIN_DOMAIN + Statistics.RESPONSE_CODE_SUCCESS);

        List<Statistics> statistics = EmailJob.process(configuration).toCompletableFuture().join();
        assertThat(statistics).isNotEmpty();
        Statistics statistic = statistics.get(0);
        assertThat(statistic.getProcessed()).isEqualTo(2);
        assertThat(statistic.getSentSuccessfully()).isEqualTo(2);
        assertThat(statistic.getTemporaryErrors()).isEqualTo(0);
        assertThat(statistic.getPermanentErrors()).isEqualTo(0);
    }

    @Test
    public void process_withMixedStateEmailObjectsAndProcessAllIsFalse_shouldNotProcessAll() {
        configuration.getTenants().get(0).setProcessAll(false);
        createCustomObject(STATUS_PENDING, "1");
        createCustomObject(STATUS_PENDING, "2");
        createCustomObject(EMAIL_STATUS_ERROR, "3");
        createCustomObject(EMAIL_STATUS_ERROR, "4");
        configuration.getTenants().get(0).setEndpointUrl(HTTPBIN_DOMAIN + Statistics.RESPONSE_CODE_SUCCESS);

        List<Statistics> statistics = EmailJob.process(configuration).toCompletableFuture().join();
        assertThat(statistics).isNotEmpty();
        Statistics statistic = statistics.get(0);
        assertThat(statistic.getProcessed()).isEqualTo(2);
        assertThat(statistic.getSentSuccessfully()).isEqualTo(2);
        assertThat(statistic.getTemporaryErrors()).isEqualTo(0);
        assertThat(statistic.getPermanentErrors()).isEqualTo(0);
    }

    @Test
    public void process_withMixedStateEmailObjectsAndProcessAllIsTrue_shouldProcessAl() {
        configuration.getTenants().get(0).setProcessAll(true);
        createCustomObject(STATUS_PENDING, "1");
        createCustomObject(STATUS_PENDING, "2");
        createCustomObject(EMAIL_STATUS_ERROR, "3");
        createCustomObject(EMAIL_STATUS_ERROR, "4");
        configuration.getTenants().get(0).setEndpointUrl(HTTPBIN_DOMAIN + Statistics.RESPONSE_CODE_SUCCESS);

        List<Statistics> statistics = EmailJob.process(configuration).toCompletableFuture().join();
        assertThat(statistics).isNotEmpty();
        Statistics statistic = statistics.get(0);
        assertThat(statistic.getProcessed()).isEqualTo(4);
        assertThat(statistic.getSentSuccessfully()).isEqualTo(4);
        assertThat(statistic.getTemporaryErrors()).isEqualTo(0);
        assertThat(statistic.getPermanentErrors()).isEqualTo(0);
    }

    @Test
    public void process_withProcessAllFlagSet_shouldReturnNoError() {
        createCustomObject(STATUS_PENDING, "1");
        createCustomObject(STATUS_PENDING, "2");
        createCustomObject(EMAIL_STATUS_ERROR, "3");
        configuration.getTenants().get(0).setEndpointUrl(HTTPBIN_DOMAIN + Statistics.RESPONSE_CODE_SUCCESS);
        configuration.getTenants().get(0).setProcessAll(true);

        final List<Statistics> statistics = EmailJob.process(configuration).toCompletableFuture().join();
        assertThat(statistics).isNotEmpty();
        Statistics statistic = statistics.get(0);
        assertThat(statistic.getProcessed()).isEqualTo(3);
        assertThat(statistic.getSentSuccessfully()).isEqualTo(3);
        assertThat(statistic.getTemporaryErrors()).isEqualTo(0);
        assertThat(statistic.getPermanentErrors()).isEqualTo(0);
    }

    @Test
    public void process_withIncorrectEndpointUrl_shouldReturnEmptyStatics() {
        createCustomObject(STATUS_PENDING, "1");
        createCustomObject(STATUS_PENDING, "2");
        createCustomObject(EMAIL_STATUS_ERROR, "3");
        configuration.getTenants().get(0).setEndpointUrl("https://unknownEndpoint.de");

        final List<Statistics> statistics = EmailJob.process(configuration).toCompletableFuture().join();
        assertThat(statistics).isNotEmpty();
        final Statistics statistic = statistics.get(0);
        statistic.print(LOG);
        assertThat(statistic.getProcessed()).isEqualTo(2);
        assertThat(statistic.getSentSuccessfully()).isEqualTo(0);
        assertThat(statistic.getTemporaryErrors()).isEqualTo(2);
        assertThat(statistic.getPermanentErrors()).isEqualTo(0);
    }

    @Test
    public void process_withUnsuccessfulEmail_shouldReturnPermenantError() {
        createCustomObject(STATUS_PENDING, "1");
        createCustomObject(STATUS_PENDING, "2");
        createCustomObject(EMAIL_STATUS_ERROR, "3");
        configuration.getTenants().get(0).setEndpointUrl(HTTPBIN_DOMAIN + Statistics.RESPONSE_ERROR_PERMANENT);

        final List<Statistics> statistics = EmailJob.process(configuration).toCompletableFuture().join();
        assertThat(statistics).isNotEmpty();
        final Statistics statistic = statistics.get(0);
        assertThat(statistic.getProcessed()).isEqualTo(2);
        assertThat(statistic.getSentSuccessfully()).isEqualTo(0);
        assertThat(statistic.getTemporaryErrors()).isEqualTo(0);
        assertThat(statistic.getPermanentErrors()).isEqualTo(2);
    }

    @Test
    public void process_withUnsuccessfulEmail_shouldReturnTemporaryError() {
        createCustomObject(STATUS_PENDING, "1");
        createCustomObject(STATUS_PENDING, "2");
        createCustomObject(EMAIL_STATUS_ERROR, "3");
        configuration.getTenants().get(0).setEndpointUrl(HTTPBIN_DOMAIN + Statistics.RESPONSE_ERROR_TEMP);

        final List<Statistics> statistics = EmailJob.process(configuration).toCompletableFuture().join();
        assertThat(statistics).isNotEmpty();
        final Statistics statistic = statistics.get(0);
        assertThat(statistic.getProcessed()).isEqualTo(2);
        assertThat(statistic.getSentSuccessfully()).isEqualTo(0);
        assertThat(statistic.getTemporaryErrors()).isEqualTo(2);
        assertThat(statistic.getPermanentErrors()).isEqualTo(0);
    }

    @Test
    public void process_withMultiTenants_shouldReturnNoError() {
        final TenantConfiguration firstTenant = configuration.getTenants().get(0);
        final TenantConfiguration secondTenant = configuration.getTenants().get(1);
        firstTenant.setEndpointUrl(HTTPBIN_DOMAIN + Statistics.RESPONSE_CODE_SUCCESS);
        secondTenant.setEndpointUrl(HTTPBIN_DOMAIN + Statistics.RESPONSE_ERROR_PERMANENT);
        configuration.getTenants().add(secondTenant);
        createCustomObject(STATUS_PENDING, "1");
        createCustomObject(STATUS_PENDING, "2");
        createCustomObject(EMAIL_STATUS_ERROR, "3");

        List<Statistics> statistics = EmailJob.process(configuration).toCompletableFuture().join();
        assertThat(statistics).isNotEmpty();
        Statistics statistic = statistics.get(0);
        assertThat(statistic.getProcessed()).isEqualTo(2);
        assertThat(statistic.getSentSuccessfully()).isEqualTo(2);
        assertThat(statistic.getTemporaryErrors()).isEqualTo(0);
        assertThat(statistic.getPermanentErrors()).isEqualTo(0);

        statistic = statistics.get(1);
        assertThat(statistic.getProcessed()).isEqualTo(2);
        assertThat(statistic.getSentSuccessfully()).isEqualTo(0);
        assertThat(statistic.getTemporaryErrors()).isEqualTo(0);
        assertThat(statistic.getPermanentErrors()).isEqualTo(2);
    }

    private void createCustomObject(final String status, final String errorMailId) {
        JsonNode jsonNode = SphereJsonUtils.parse(String.format("{\"status\":\"%s\"}", status));
        CustomObjectDraft<JsonNode> draft = CustomObjectDraft.ofUnversionedUpsert(EmailProcessor.CONTAINER_ID,
                errorMailId, jsonNode);
        ctpClient.execute(CustomObjectUpsertCommand.of(draft)).toCompletableFuture().join();

    }

    /**
     * Applies the {@code pageMapper} function on each page fetched from the supplied {@code queryRequestSupplier} on
     * the supplied {@code ctpClient}.
     *
     * @param ctpClient            defines the CTP project to apply the query on.
     * @param queryRequestSupplier defines a supplier which, when executed, returns the query that should be
     *                             made on
     *                             the CTP project.
     * @param resourceMapper       defines a mapper function that should be applied on each resource in the
     *                             fetched page
     *                             from the query on the specified CTP project.
     */
    void queryAndApply(@Nonnull final SphereClient ctpClient,
                       @Nonnull final Supplier<CustomObjectQuery<JsonNode>> queryRequestSupplier,
                       @Nonnull final Function<CustomObject<JsonNode>, SphereRequest<CustomObject<JsonNode>>>
                               resourceMapper) {
        queryAll(ctpClient, queryRequestSupplier.get(), resourceMapper)
                .thenApply(allRequests -> allRequests.stream()
                        .map(ctpClient::execute)
                        .map(CompletionStage::toCompletableFuture).collect(toList()))
                .thenApply(list -> list.toArray(new CompletableFuture[list.size()]))
                .thenCompose(CompletableFuture::allOf)
                .toCompletableFuture().join();
    }
}

