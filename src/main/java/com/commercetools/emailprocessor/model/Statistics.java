package com.commercetools.emailprocessor.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


public class Statistics {
    private static final Logger LOG = LoggerFactory.getLogger(Statistics.class);
    public static final int RESPONSE_CODE_SUCCESS = 201;
    public static final int RESPONSE_ERROR_TEMP = 200;

    public Statistics() {
    }

    int sendEmails = 0;
    int successful = 0;
    int permantError = 0;
    int temperror = 0;

    public int getSendEmails() {
        return sendEmails;
    }

    public void setSendEmails(int sendEmails) {
        this.sendEmails = sendEmails;
    }

    public int getSuccessful() {
        return successful;
    }

    public void setSuccessful(int successful) {
        this.successful = successful;
    }

    public int getPermantError() {
        return permantError;
    }

    public void setPermantError(int permantError) {
        this.permantError = permantError;
    }

    public int getTemperror() {
        return temperror;
    }

    public void setTemperror(int temperror) {
        this.temperror = temperror;
    }

    public void update(int httpStatusCode) {
        sendEmails++;
        switch (httpStatusCode) {
            case RESPONSE_CODE_SUCCESS:
                successful++;
                break;
            case RESPONSE_ERROR_TEMP:
                temperror++;
                break;
            default:
                permantError++;
                break;
        }
    }

    public void print() {
        LOG.info("# processed Email " + sendEmails);
        LOG.info("# processed successfull " + successful);
        LOG.info("# processed with temporal error " + temperror);
        LOG.info("# processed with permanent error " + permantError);
}


}
