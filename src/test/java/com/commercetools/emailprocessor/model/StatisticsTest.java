package com.commercetools.emailprocessor.model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class StatisticsTest {

    Statistics statistics = null;

    @Before
    public void setUp() throws Exception {
        statistics = new Statistics();

    }

    @Test
    public void getProcessedEmail() throws Exception {
        assertEquals(statistics.getProcessedEmails(), 0);
    }

    @Test

    public void setProcessedEmail() throws Exception {
        statistics.setProcessedEmails(2);
        assertEquals(statistics.getProcessedEmails(), 2);
    }

    @Test
    public void getSuccessful() throws Exception {
        assertEquals(statistics.getSuccessfulSendedEmails(), 0);
    }

    @Test
    public void setSuccessful() throws Exception {
        statistics.setSuccessfulSendedEmails(5);
        assertEquals(statistics.getSuccessfulSendedEmails(), 5);
    }

    @Test
    public void getPermanentError() throws Exception {
        assertEquals(statistics.getPermanentErrors(),0);
    }

    @Test
    public void setPermanentError() throws Exception {
        statistics.setPermanentErrors(8);
        assertEquals(statistics.getPermanentErrors(), 8);
    }

    @Test
    public void getTempError() throws Exception {
        assertEquals(statistics.getTemporarilyErrors(), 0);
    }

    @Test
    public void setTempError() throws Exception {
        statistics.setTemporarilyErrors(3);
        assertEquals(statistics.getTemporarilyErrors(), 3);
    }

    @Test
    public void update() throws Exception {
        statistics.update(Statistics.RESPONSE_CODE_SUCCESS);
        statistics.update(Statistics.RESPONSE_CODE_SUCCESS);
        statistics.update(Statistics.RESPONSE_ERROR_TEMP);
        statistics.update(Statistics.RESPONSE_ERROR_TEMP);
        statistics.update(Statistics.RESPONSE_ERROR_TEMP);
        statistics.update(Statistics.RESPONSE_CODE_SUCCESS);
        statistics.update(Statistics.RESPONSE_ERROR_TEMP);
        statistics.update(Statistics.RESPONSE_ERROR_PERMANENT);

        assertEquals(statistics.getSuccessfulSendedEmails(), 3);
        assertEquals(statistics.getPermanentErrors(), 1);
        assertEquals(statistics.getTemporarilyErrors(), 4);
        assertEquals(statistics.getProcessedEmails(), 8);
    }

    @Test
    public void print() throws Exception {
    }

}