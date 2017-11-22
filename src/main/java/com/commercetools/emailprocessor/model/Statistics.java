package com.commercetools.emailprocessor.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Statistics {
    public static final int RESPONSE_CODE_SUCCESS = 200;
    public static final int RESPONSE_ERROR_TEMP = 503;
    public static final int RESPONSE_ERROR_PERMANENT = 400;
    public String tenantID = "";
    private int processedEmails = 0;
    private int notProcessedEmails = 0;
    private int successfulSendedEmails = 0;
    private int permanentErrors = 0;
    private int temporarilyErrors = 0;

    public Statistics() {

    }

    public Statistics(String tenant) {
        tenantID = tenant;
    }


    public String getTenantID() {
        return tenantID;
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
            case 0:
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

    @JsonIgnore
    public void print(Logger logger) {
        try {
            logger.info(getStatisticsAsJSONString());
        } catch (JsonProcessingException e) {
            logger.error("Cannot create json statistics.", e);
        }
    }


}
