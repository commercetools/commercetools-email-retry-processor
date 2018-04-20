package com.commercetools.emailprocessor.email;


import com.commercetools.emailprocessor.model.Statistics;
import com.commercetools.emailprocessor.model.TenantConfiguration;
import com.fasterxml.jackson.databind.JsonNode;
import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.customobjects.CustomObject;
import io.sphere.sdk.customobjects.queries.CustomObjectQuery;
import io.sphere.sdk.queries.QueryPredicate;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Function;

import static io.sphere.sdk.queries.QueryExecutionUtils.queryAll;
import static java.lang.String.format;
import static java.util.concurrent.CompletableFuture.allOf;

public class EmailProcessor {
    public static final String CONTAINER_ID = "unprocessedEmail";
    public static final String STATUS_PENDING = "pending";
    public static final String EMAIL_STATUS_ERROR = "error";
    static final String PARAM_EMAIL_ID = "emailid";
    static final String PARAM_TENANT_ID = "tenantid";
    private static final String ENCRYPTION_ALGORITHM = "Blowfish";
    /**
     * Limited thread pool where to execute {@link #callApiEndpoint(String, TenantConfiguration)} and all chained post
     * processor. For now limited for up to 8 parallel requests.
     */
    private static final Executor callApiThreadPool = Executors.newWorkStealingPool(8);
    private static final Logger LOG = LoggerFactory.getLogger(EmailProcessor.class);

    public static EmailProcessor of() {
        return new EmailProcessor();
    }

    /**
     * Iterate through all Email objects and triggers the webhook for each pending email object.
     *
     * @param tenantConfig Configuration of a tenant
     * @return Statics of the sent emails
     */
    public CompletionStage<Statistics> processEmails(final TenantConfiguration tenantConfig) {
        final SphereClient client = tenantConfig.getSphereClient();
        CustomObjectQuery<JsonNode> query = CustomObjectQuery.ofJsonNode().byContainer(CONTAINER_ID);
        if (!tenantConfig.isProcessAll()) {
            query = query.plusPredicates(QueryPredicate.of(format("value(status=\"%s\")", STATUS_PENDING)));
        }
        // We sort the email object by creation time, to ensure that the emails are delivered in the correct
        // chronological order they were sent with.
        query = query.withSort(s -> s.createdAt().sort().asc());
        final Statistics statistics = new Statistics(tenantConfig.getProjectKey());

        final Function<CustomObject<JsonNode>, CompletableFuture<Void>> customObjectMapper = customObject ->
                callApiEndpoint(customObject.getId(), tenantConfig)
                        .thenAccept(statistics::update)
                        .toCompletableFuture();


        // 1. Query for all custom objects and map each to the future chain above.
        // 2. Map the list of future to an allOf future to execute them in parallel.
        return queryAll(client, query, customObjectMapper)
                .thenCompose(stages -> allOf(stages.toArray(new CompletableFuture[stages.size()])))
                .handle(((voidResult, exception) -> {
                    client.close();
                    if (exception != null) {
                        LOG.error(format("[Tenant Project key: %s] An error occurred while "
                                        + "processing custom objects.",
                                tenantConfig.getProjectKey()), exception);
                        return Statistics.ofError(tenantConfig.getProjectKey());
                    }
                    return statistics;
                }));
    }

    /**
     * Sends a post request to a api endpoint.
     *
     * @param customObjectId      ID of a custom object, which contains a email
     * @param tenantConfiguration Configuration of the current tenant
     * @return Http Status code response code of the current request
     */
    CompletableFuture<Integer> callApiEndpoint(@Nonnull final String customObjectId,
                                               @Nonnull final TenantConfiguration tenantConfiguration) {
        return CompletableFuture.supplyAsync(() -> {
            final HttpPost httpPost = tenantConfiguration.getHttpPost();
            final List<NameValuePair> params = new ArrayList<>();
            try {
                final String encryptedCustomerId = blowFish(customObjectId, tenantConfiguration.getEncryptionKey(),
                        Cipher.ENCRYPT_MODE);
                params.add(new BasicNameValuePair(PARAM_EMAIL_ID, encryptedCustomerId));
                params.add(new BasicNameValuePair(PARAM_TENANT_ID, tenantConfiguration.getProjectKey()));
                httpPost.setEntity(new UrlEncodedFormEntity(params, Charset.defaultCharset()));
                return doPost(HttpClients.createDefault(), httpPost, tenantConfiguration.getProjectKey());
            } catch (Exception exception) {
                LOG.error("Cannot trigger the endpoint", exception);
                return Statistics.RESPONSE_ERROR_TEMP;
            }
        }, callApiThreadPool);
    }

    /**
     * Execute the final post request.
     *
     * @param httpClient current httpClient
     * @param httpPost   current post
     * @param projectKey current projectKey
     * @return http status code
     * @throws IOException when the post request fails
     */
    int doPost(final CloseableHttpClient httpClient, final HttpPost httpPost, final String projectKey)
            throws IOException {
        try (final CloseableHttpResponse response = httpClient.execute(httpPost)) {
            if (response.getStatusLine() != null) {
                return response.getStatusLine().getStatusCode();
            } else {
                LOG.error(format("[%s] The Statuscode of the current api call cannot be retrieved", projectKey));
                return Statistics.RESPONSE_ERROR_PERMANENT;
            }
        }
    }

    /**
     * Encrypt / decrypt value using the blowfish algorithm.
     *
     * @param value      Value to Encrypt / decrypt
     * @param key        key for encryption/decryption
     * @param cipherMode cipher mode
     * @return modified value or null if something went wrong.
     */
    String blowFish(@Nonnull final String value, @Nonnull final String key, final int cipherMode)
            throws GeneralSecurityException {
        final byte[] keyData = key.getBytes(Charset.forName("UTF-8"));
        final SecretKeySpec ks = new SecretKeySpec(keyData, ENCRYPTION_ALGORITHM);
        final Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        cipher.init(cipherMode, ks);
        final byte[] encrypted = cipher.doFinal(value.getBytes(Charset.forName("UTF-8")));
        return Base64.encodeBase64String(encrypted);
    }


}