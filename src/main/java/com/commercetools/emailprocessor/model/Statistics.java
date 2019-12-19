package com.commercetools.emailprocessor.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;

import static java.lang.String.format;
import static net.logstash.logback.marker.Markers.appendRaw;


public class Statistics {
    public static final int RESPONSE_CODE_SUCCESS = 200;
    public static final int RESPONSE_ERROR_TEMP = 503;
    public static final int RESPONSE_ERROR_PERMANENT = 400;
    private String tenantId = "";
    private int globalError = 0;
    private int processed = 0;
    private int sentSuccessfully = 0;
    private int permanentErrors = 0;
    private int temporaryErrors = 0;

    public Statistics() {
    }

    public Statistics(final String tenant) {
        tenantId = tenant;
    }


    /**
     * Generates a statistic object for the error case.
     *
     * @param tenant Tenant, to which this static belongs
     * @return Statistic, with global error set to 1.
     */
    public static Statistics ofError(final String tenant) {
        Statistics statistics = new Statistics(tenant);
        statistics.globalError = 1;
        return statistics;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(final String tenantId) {
        this.tenantId = tenantId;
    }

    public int getGlobalError() {
        return globalError;
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

    public int getTemporaryErrors() {
        return temporaryErrors;
    }

    /**
     * Update the statistics depending on the httpStatusCode.
     *
     * <p>This method is <i>synchronized</i> to allow update tenant statistic concurrently in parallel threads.
     * Considering it is very simple method and it is called rarely -
     * no need to optimize concurrency more than it is.</p>
     *
     * @param httpStatusCode returned http status code of the api call.
     */
    @JsonIgnore
    public synchronized void update(int httpStatusCode) {
        processed++;
        switch (httpStatusCode) {
            case RESPONSE_CODE_SUCCESS:
                sentSuccessfully++;
                break;
            case RESPONSE_ERROR_TEMP:
                temporaryErrors++;
                break;
            case RESPONSE_ERROR_PERMANENT:
                permanentErrors++;
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
    public synchronized void print(final Logger logger) {
        if (processed > 0) {
            try {
                logger.info(appendRaw("statistics", getStatisticsAsJSONString()),
                    format("Statistics for tenant with id [%s]", this.getTenantId()));
            } catch (JsonProcessingException exception) {
                logger.error("Cannot create json statistics.", exception);
            }
        }
    }
}
