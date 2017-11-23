package com.commercetools.emailprocessor.email;


import com.commercetools.emailprocessor.model.Statistics;
import com.commercetools.emailprocessor.model.TenantConfiguration;
import com.fasterxml.jackson.databind.JsonNode;
import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.customobjects.CustomObject;
import io.sphere.sdk.customobjects.queries.CustomObjectQuery;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.util.concurrent.CompletionStage;

public class EmailProcessor {


    public static final String CONTAINER_ID = "unprocessedEmail";
    public static final String EMAIL_PROPERTY_STATUS = "status";
    public static final String EMAIL_STATUS_PENDING = "pending";
    public static final String EMAIL_STATUS_ERROR = "error";
    private static final Logger LOG = LoggerFactory.getLogger(EmailProcessor.class);
    private static final String PARAM_EMAIL_ID = "emailid";
    private static final String PARAM_TENANT_ID = "tenantid";

    public EmailProcessor() {
    }

    /**
     * Iterate through all Email objects and triggers the webhook for each  pending email object.
     *
     * @param tenantConfiguration Configuration of a tenant
     * @return Statics of the sended emails
     */

    public CompletionStage<Statistics> processEmails(final TenantConfiguration tenantConfiguration) {
        SphereClient client = tenantConfiguration.getSphereClient();
        CustomObjectQuery<JsonNode> query = CustomObjectQuery.ofJsonNode();
        query = query.byContainer(CONTAINER_ID).withLimit(100L).withSort(s -> s.createdAt().sort().asc());
        return client.execute(query).thenApply(response -> {
                Statistics statistics = new Statistics(tenantConfiguration.getProjectKey());
                if (response.getTotal() < 1) {
                    LOG.error(String.format("No email to process for tenant %s", tenantConfiguration
                        .getProjectKey()));
                }
                for (CustomObject<JsonNode> customObject : response.getResults()) {
                    JsonNode email = customObject.getValue();
                    String status = email != null && email.get(EMAIL_PROPERTY_STATUS) != null ? email.get(
                        EMAIL_PROPERTY_STATUS)
                        .asText() : "";
                    if (StringUtils.equalsIgnoreCase(status, EMAIL_STATUS_PENDING)) {
                        int httpStatusCode = 0;
                        try {
                            httpStatusCode = callApiEndpoint(customObject.getId(), tenantConfiguration);
                        } catch (Exception exeception) {
                            LOG.error("Cannot call endpoint", exeception);
                        }
                        statistics.update(httpStatusCode);
                    } else {
                        statistics.update(0);
                    }
                }
                return statistics;
            }

        );

    }

    /**
     * Sends a post request to a api endpoint.
     *
     * @param customobjectid      ID of a customobject, which constains a email
     * @param tenantConfiguration Configuration of the current tenant
     * @return Http Status code response code of the current request
     */
    int callApiEndpoint(final String customobjectid, final TenantConfiguration tenantConfiguration) throws Exception {
        HttpURLConnection httpUrlConnectionConnection = tenantConfiguration.getHttpUrlConnection();
        httpUrlConnectionConnection.setRequestMethod("POST");
        httpUrlConnectionConnection.setDoOutput(true);
        String params = String.format(PARAM_EMAIL_ID + "=%s&" + PARAM_TENANT_ID + "=%s", customobjectid,
            tenantConfiguration.getProjectKey());
        IOUtils.write(params, httpUrlConnectionConnection.getOutputStream(), Charset.defaultCharset());
        int responseCode = httpUrlConnectionConnection.getResponseCode();
        IOUtils.close(httpUrlConnectionConnection);

        return responseCode;
    }
}
