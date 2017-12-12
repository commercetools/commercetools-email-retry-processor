package com.commercetools.emailprocessor.email;


import com.commercetools.emailprocessor.model.Statistics;
import com.commercetools.emailprocessor.model.TenantConfiguration;
import com.fasterxml.jackson.databind.JsonNode;
import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.customobjects.CustomObject;
import io.sphere.sdk.customobjects.queries.CustomObjectQuery;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public class EmailProcessor {

    public static final String CONTAINER_ID = "unprocessedEmail";
    public static final String EMAIL_PROPERTY_STATUS = "status";
    public static final String EMAIL_STATUS_PENDING = "pending";
    public static final String EMAIL_STATUS_ERROR = "error";
    public static final int STATUS_UNPROCESS = 0;
    public static final String ENCRYPTION_ALGORITHM = "Blowfish";
    static final String PARAM_EMAIL_ID = "emailid";
    static final String PARAM_TENANT_ID = "tenantid";
    private static final Logger LOG = LoggerFactory.getLogger(EmailProcessor.class);
    private List<NameValuePair> params;

    public EmailProcessor() {
    }

    /**
     * Iterate through all Email objects and triggers the webhook for each pending email object.
     *
     * @param tenantConfiguration Configuration of a tenant
     * @return Statics of the sent emails
     */
    public CompletionStage<Statistics> processEmails(final TenantConfiguration tenantConfiguration) {
        SphereClient client = tenantConfiguration.getSphereClient();
        CustomObjectQuery<JsonNode> query = CustomObjectQuery.ofJsonNode().byContainer(CONTAINER_ID).withLimit(100L)
            .withSort(s -> s.createdAt().sort().asc());
        return client
            .execute(query)
            .thenApply(response -> {
                Statistics statistics = new Statistics(tenantConfiguration.getProjectKey());
                if (response.getTotal() < 1) {
                    LOG.info(String.format("No email to process for tenant %s", tenantConfiguration
                        .getProjectKey()));
                }
                for (CustomObject<JsonNode> customObject : response.getResults()) {
                    String status = Optional.ofNullable(customObject)
                        .map(CustomObject::getValue)
                        .map(node -> node.get(EMAIL_PROPERTY_STATUS))
                        .map(JsonNode::asText)
                        .orElse("");
                    if (StringUtils.equalsIgnoreCase(status, EMAIL_STATUS_PENDING) || tenantConfiguration
                        .isProcessAll()) {
                        int httpStatusCode = STATUS_UNPROCESS;
                        try {
                            httpStatusCode = callApiEndpoint(customObject.getId(), tenantConfiguration);
                        } catch (Exception exception) {
                            LOG.error(String.format("[%s] Cannot call endpoint", tenantConfiguration.getProjectKey()),
                                exception);
                        }
                        statistics.update(httpStatusCode);
                    } else {
                        statistics.update(STATUS_UNPROCESS);
                    }
                }
                client.close();
                return statistics;
            })
            .exceptionally(exception -> {
                LOG.error(String.format("[%s] An unknown error occurred", tenantConfiguration.getProjectKey()),
                    exception);
                client.close();
                return new Statistics(tenantConfiguration.getProjectKey());
            });
    }

    /**
     * Sends a post request to a api endpoint.
     *
     * @param customObjectId      ID of a customobject, which constains a email
     * @param tenantConfiguration Configuration of the current tenant
     * @return Http Status code response code of the current request
     */
    int callApiEndpoint(@Nonnull final String customObjectId, @Nonnull final TenantConfiguration tenantConfiguration)
        throws Exception {
        CloseableHttpResponse response = null;
        try {
            final HttpPost httpPost = tenantConfiguration.getHttpPost();
            final List<NameValuePair> params = new ArrayList<>();
            String encyptedCustomerId = blowFish(customObjectId, tenantConfiguration.getEncryptionKey(), Cipher
                .ENCRYPT_MODE);
            params.add(new BasicNameValuePair(PARAM_EMAIL_ID, encyptedCustomerId));
            params.add(new BasicNameValuePair(PARAM_TENANT_ID, tenantConfiguration.getProjectKey()));
            httpPost.setEntity(new UrlEncodedFormEntity(params, Charset.defaultCharset()));
            response = HttpClients.createDefault().execute(httpPost);
            return response.getStatusLine() != null ? response.getStatusLine().getStatusCode() : Statistics
                .RESPONSE_ERROR_PERMANENT;
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    /**
     * Encrypt / decrypt value using the blowfish algorithm.
     *
     * @param value      Value to Encrypt / decrypt
     * @param key        key for encryption/decryption
     * @param cipherMode ciphermode
     * @return modified value or null if something went wrong.
     */
    String blowFish(@Nonnull final String value, @Nonnull final String key, @Nonnull final int cipherMode) throws
        Exception {
        final byte[] keyData = key.getBytes(Charset.forName("UTF-8"));
        final SecretKeySpec ks = new SecretKeySpec(keyData, ENCRYPTION_ALGORITHM);
        final Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        cipher.init(cipherMode, ks);
        final byte[] encrypted = cipher.doFinal(value.getBytes(Charset.forName("UTF-8")));
        return Base64.encodeBase64String(encrypted);
    }
}