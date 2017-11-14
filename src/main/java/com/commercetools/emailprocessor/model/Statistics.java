package com.commercetools.emailprocessor.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Statistics {
    private static final Logger LOG = LoggerFactory.getLogger(Statistics.class);
    public static final int RESPONSE_CODE_SUCCESS = 201;
    public static final int RESPONSE_ERROR_TEMP = 200;
    public static final int RESPONSE_ERROR_PERMANENT=400;
    public Statistics() {
    }

    int processedEmail = 0;
    int successful = 0;
    int permanentError = 0;
    int tempError = 0;

    public int getProcessedEmail() {
        return processedEmail;
    }

    public void setProcessedEmail(int processedEmail) {
        this.processedEmail = processedEmail;
    }

    public int getSuccessful() {
        return successful;
    }

    public void setSuccessful(int successful) {
        this.successful = successful;
    }

    public int getPermanentError() {
        return permanentError;
    }

    public void setPermanentError(int permanentError) {
        this.permanentError = permanentError;
    }

    public int getTempError() {
        return tempError;
    }

    public void setTempError(int tempError) {
        this.tempError = tempError;
    }

    public void update(int httpStatusCode) {
        processedEmail++;
        switch (httpStatusCode) {
            case RESPONSE_CODE_SUCCESS:
                successful++;
                break;
            case RESPONSE_ERROR_TEMP:
                tempError++;
                break;
            case RESPONSE_ERROR_PERMANENT:
                permanentError++;
                break;
            default:
                permanentError++;
                break;
        }
    }

    public void print() {
        LOG.info("# processed Email " + processedEmail);
        LOG.info("# processed successfull " + successful);
        LOG.info("# processed with temporal error " + tempError);
        LOG.info("# processed with permanent error " + permanentError);
}


}
