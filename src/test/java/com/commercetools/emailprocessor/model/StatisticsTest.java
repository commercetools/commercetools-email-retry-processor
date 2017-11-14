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
        assertEquals(statistics.getProcessedEmail(), 0);
    }

    @Test

    public void setProcessedEmail() throws Exception {
        statistics.setProcessedEmail(2);
        assertEquals(statistics.getProcessedEmail(), 2);
    }

    @Test
    public void getSuccessful() throws Exception {
        assertEquals(statistics.getSuccessful(), 0);
    }

    @Test
    public void setSuccessful() throws Exception {
        statistics.setSuccessful(5);
        assertEquals(statistics.getSuccessful(), 5);
    }

    @Test
    public void getPermanentError() throws Exception {
        assertEquals(statistics.getPermanentError(),0);
    }

    @Test
    public void setPermanentError() throws Exception {
        statistics.setPermanentError(8);
        assertEquals(statistics.getPermanentError(), 8);
    }

    @Test
    public void getTempError() throws Exception {
        assertEquals(statistics.getTempError(), 0);
    }

    @Test
    public void setTempError() throws Exception {
        statistics.setTempError(3);
        assertEquals(statistics.getTempError(), 3);
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

        assertEquals(statistics.getSuccessful(), 3);
        assertEquals(statistics.getPermanentError(), 1);
        assertEquals(statistics.getTempError(), 4);
        assertEquals(statistics.getProcessedEmail(), 8);
    }

    @Test
    public void print() throws Exception {
    }

}