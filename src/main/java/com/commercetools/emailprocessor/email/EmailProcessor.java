package com.commercetools.emailprocessor.email;

import com.commercetools.emailprocessor.model.Email;
import com.commercetools.emailprocessor.model.Statistics;
import com.commercetools.emailprocessor.model.TenantConfiguration;
import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.customobjects.CustomObject;
import io.sphere.sdk.customobjects.queries.CustomObjectQuery;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.net.HttpURLConnection;

public class EmailProcessor {


    private static final Logger LOG = LoggerFactory.getLogger(EmailProcessor.class);
    private static final String PARAM_EMAIL_ID = "emailid";
    private static final String PARAM_TENANT_ID = "tenantid";
    private static final String CONTAINER_ID = "unprocessedEmail";
  ;
    private static final String EMAIL_STATUS_PENDING = "pending";

    public EmailProcessor() {
    }

    /**
     * Iterate through all Email objects and triggers the webhook for each  pending email object
     *
     * @param tenantConfiguration Configuration of a tenant
     * @return Statics of the sended emails
     */

    public Statistics processEmails(TenantConfiguration tenantConfiguration) {

        SphereClient client = tenantConfiguration.getSphereClient();
        CustomObjectQuery<Email> query = CustomObjectQuery.of(Email.class);
        query = query.byContainer(CONTAINER_ID);
        return client.execute(query).thenApply(response -> {
                    Statistics statistics = new Statistics();
                    if (response.getTotal() < 1) {
                        LOG.error(String.format("No email to process for tenant %s", tenantConfiguration
                                .getProjectKey()));
                    }
                    for (CustomObject<Email> customObject : response.getResults()) {
                        Email email = customObject.getValue();
                        String status = email != null?email.getStatus(): "";
                        if (StringUtils.equalsIgnoreCase(status, EMAIL_STATUS_PENDING)) {
                            int httpStatusCode = callWebHook(customObject.getId(), tenantConfiguration);
                            statistics.update(httpStatusCode);

                        }
                    }
                    return statistics;
                }

        ).toCompletableFuture().join();

    }

    /**
     * Sends a post request to a webhook
     *
     * @param customObjectID       ID of a customobject, which constains a email
     * @param tenantConfiguration
     * @return Http Status code
     */
    int callWebHook(String customObjectID, TenantConfiguration tenantConfiguration) {
        int responseCode = HttpStatus.SC_OK;
        OutputStream outputStream = null;
        try {
            HttpURLConnection httpURLConnection =tenantConfiguration.getHttpURLConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);
            String params = String.format(PARAM_EMAIL_ID + "=%s&" + PARAM_TENANT_ID + "=%s", customObjectID,
                    tenantConfiguration.getProjectKey());
           IOUtils.write(params,httpURLConnection.getOutputStream());

            responseCode = httpURLConnection.getResponseCode();
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            LOG.error("The webhook cannot be called", e);
        }
        return responseCode;
    }
}
