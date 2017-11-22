package com.commercetools.emailprocessor.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Statistics {
    private static final Logger LOG = LoggerFactory.getLogger(Statistics.class);
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

    public void setTenantID(String tenantID) {
        this.tenantID = tenantID;
    }

    public int getNotProcessedEmails() {
        return notProcessedEmails;
    }

    public void setNotProcessedEmails(int notProcessedEmails) {
        this.notProcessedEmails = notProcessedEmails;
    }

    public int getProcessedEmails() {
        return processedEmails;
    }

    public void setProcessedEmails(int processedEmails) {
        this.processedEmails = processedEmails;
    }

    public int getSuccessfulSendedEmails() {
        return successfulSendedEmails;
    }

    public void setSuccessfulSendedEmails(int successfulSendedEmails) {
        this.successfulSendedEmails = successfulSendedEmails;
    }

    public int getPermanentErrors() {
        return permanentErrors;
    }

    public void setPermanentErrors(int permanentErrors) {
        this.permanentErrors = permanentErrors;
    }

    public int getTemporarilyErrors() {
        return temporarilyErrors;
    }

    public void setTemporarilyErrors(int temporarilyErrors) {
        this.temporarilyErrors = temporarilyErrors;
    }

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
            default:
                permanentErrors++;
                break;
        }
    }

    public void print() {
        LOG.info("##########################");
        LOG.info(String.format("Processing statistic for tenant %s", tenantID));
        LOG.info("# processed Emails " + processedEmails);
        LOG.info("# not processed Emails " + notProcessedEmails);
        LOG.info("# processed successfull " + successfulSendedEmails);
        LOG.info("# processed with temporal error " + temporarilyErrors);
        LOG.info("# processed with permanent error " + permanentErrors);
        LOG.info("##########################");
    }


}
