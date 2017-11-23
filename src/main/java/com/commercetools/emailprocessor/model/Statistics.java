package com.commercetools.emailprocessor.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;


public class Statistics {
    public static final int RESPONSE_CODE_SUCCESS = 200;
    public static final int RESPONSE_ERROR_TEMP = 503;
    public static final int RESPONSE_ERROR_PERMANENT = 400;
    private String tenantId = "";
    private int processedEmails = 0;
    private int notProcessedEmails = 0;
    private int successfulSendedEmails = 0;
    private int permanentErrors = 0;
    private int temporarilyErrors = 0;

    public Statistics() {

    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(final String tenantId) {
        this.tenantId = tenantId;
    }

    public Statistics(final String tenant) {
        tenantId = tenant;
    }


    public int getNotProcessedEmails() {
        return notProcessedEmails;
    }


    public int getProcessedEmails() {
        return processedEmails;
    }


    public int getSuccessfulSendedEmails() {
        return successfulSendedEmails;
    }


    public int getPermanentErrors() {
        return permanentErrors;
    }


    public int getTemporarilyErrors() {
        return temporarilyErrors;
    }

    /**
     * Update the statistic dependent on the httpStatusCode.
     *
     * @param httpStatusCode returned http status code of the api call.
     */
    @JsonIgnore
    public void update(int httpStatusCode) {
        processedEmails++;
        switch (httpStatusCode) {
            case RESPONSE_CODE_SUCCESS:
                successfulSendedEmails++;
                break;
            case RESPONSE_ERROR_TEMP:
                temporarilyErrors++;
                break;
            case RESPONSE_ERROR_PERMANENT:
                permanentErrors++;
                break;
            default:
                notProcessedEmails++;
                processedEmails--;
                break;


        }
    }

    @JsonIgnore
    public String getStatisticsAsJSONString() throws JsonProcessingException {
        final ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(this);
    }

    /**
     * Print the statistic.
      * @param logger passed logger
     */
    @JsonIgnore
    public void print(final Logger logger) {
        try {
            logger.info(getStatisticsAsJSONString());
        } catch (JsonProcessingException exception) {
            logger.error("Cannot create json statistics.", exception);
        }
    }


}
