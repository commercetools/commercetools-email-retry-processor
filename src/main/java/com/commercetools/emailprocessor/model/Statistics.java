package com.commercetools.emailprocessor.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;


public class Statistics {
    public static final int RESPONSE_CODE_SUCCESS = 200;
    public static final int RESPONSE_ERROR_TEMP = 503;
    public static final int RESPONSE_ERROR_PERMANENT = 400;
    public static final int RESPONSE_IGNORED = 0;
    private String tenantId = "";
    private int processed = 0;
    private int notProcessed = 0;
    private int sentSuccessfully = 0;
    private int permanentErrors = 0;
    private int temporarilyErrors = 0;

    public Statistics() {

    }

    public Statistics(final String tenant) {
        tenantId = tenant;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(final String tenantId) {
        this.tenantId = tenantId;
    }

    public int getNotProcessed() {
        return notProcessed;
    }


    public int getProcessed() {
        return processed;
    }


    public int getSentSuccessfully() {
        return sentSuccessfully;
    }


    public int getPermanentErrors() {
        return permanentErrors;
    }


    public int getTemporarilyErrors() {
        return temporarilyErrors;
    }

    /**
     * Update the statistics depending on the httpStatusCode.
     *
     * @param httpStatusCode returned http status code of the api call.
     */
    @JsonIgnore
    public void update(int httpStatusCode) {
        processed++;
        switch (httpStatusCode) {
            case RESPONSE_CODE_SUCCESS:
                sentSuccessfully++;
                break;
            case RESPONSE_ERROR_TEMP:
                temporarilyErrors++;
                break;
            case RESPONSE_ERROR_PERMANENT:
                permanentErrors++;
                break;
            case RESPONSE_IGNORED:
                notProcessed++;
                processed--;
                break;
            default:
                permanentErrors++;
                break;
        }
    }

    @JsonIgnore
    private String getStatisticsAsJSONString() throws JsonProcessingException {
        final ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(this);
    }

    /**
     * Print the statistic.
     *
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
