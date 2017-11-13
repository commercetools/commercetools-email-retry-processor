package com.commercetools.emailprocessor.email;

import com.commercetools.emailprocessor.model.TenantConfiguration;
import com.fasterxml.jackson.databind.JsonNode;
import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.customobjects.CustomObject;
import io.sphere.sdk.customobjects.queries.CustomObjectQuery;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

public class EmailProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(EmailProcessor.class);
    private static final String PARAM_EMAIL_ID = "emailid";
    private static final String PARAM_TENANT_ID = "tenantid";
    private static final String CONTAINER_ID = "unprocessedEmail";
    private static final String EMAIL_PROPERTY_STATUS = "status";
    private static final String EMAIL_STATUS_PENDING = "pending";

    public static final int RESPONSE_CODE_SUCCESS = 201;
    public static final int RESPONSE_ERROR_TEMP = 200;


    public static boolean processEmails(TenantConfiguration tenantConfiguration) {
        boolean success = true;
        SphereClient client = tenantConfiguration.getSphereClient();
        CustomObjectQuery<JsonNode> query = CustomObjectQuery.ofJsonNode();
        query = query.byContainer(CONTAINER_ID);
        success = client.execute(query).thenApply(r -> {
                    if (r.getTotal() < 1) {
                        LOG.error(String.format("No email to process for tenant %s", tenantConfiguration
                                .getProjectKey()));
                    }
                    boolean processed = true;
                    int sendEmails = 0;
                    int send_successfull = 0;
                    int sendErrorTemp = 0;
                    int sendErrorPermanent = 0;
                    for (CustomObject<JsonNode> customObject : r.getResults()) {
                        JsonNode email = customObject.getValue();
                        String status = email != null && email.get(EMAIL_PROPERTY_STATUS) != null ? email.get
                                (EMAIL_PROPERTY_STATUS).asText() : "";
                        if (StringUtils.equalsIgnoreCase(status, EMAIL_STATUS_PENDING)) {
                            int responseCode = sendEmail(customObject, tenantConfiguration);
                            sendEmails++;
                            switch (responseCode) {
                                case RESPONSE_CODE_SUCCESS:
                                    send_successfull++;
                                    break;
                                case RESPONSE_ERROR_TEMP:
                                    sendErrorTemp++;
                                    break;
                                default:
                                    sendErrorPermanent++;
                                    break;
                            }
                        }
                    }
                    LOG.info("##########################");
                    LOG.info(String.format("Processing statistic for tenant %s", tenantConfiguration.getProjectKey()));
                    LOG.info("# processed Email" + sendEmails);
                    LOG.info("# processed successfull" + send_successfull);
                    LOG.info("# processed with temporal error" + sendErrorTemp);
                    LOG.info("#processed with permanent error" + send_successfull);
                    LOG.info("##########################");
                    return processed;
                }

        ).toCompletableFuture().join();
        return success;
    }


     static int sendEmail(CustomObject<JsonNode> customObject, TenantConfiguration tenantConfiguration) {
        int responseCode = HttpStatus.SC_OK;
        try {
            URL postUrl = new URL(tenantConfiguration.getWebhookURL());
            HttpURLConnection con = (HttpURLConnection) postUrl.openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            OutputStream outputStream = con.getOutputStream();
            String params = String.format(PARAM_EMAIL_ID + "=%s&" + PARAM_TENANT_ID + "=%s", customObject.getId(),
                    tenantConfiguration.getProjectKey());
            outputStream.write(params.getBytes());
            outputStream.flush();
            outputStream.close();
            responseCode = con.getResponseCode();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return responseCode;
    }
}
