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
    public void update() throws Exception {
        statistics.update(Statistics.RESPONSE_CODE_SUCCESS);
        statistics.update(Statistics.RESPONSE_CODE_SUCCESS);
        statistics.update(Statistics.RESPONSE_ERROR_TEMP);
        statistics.update(Statistics.RESPONSE_ERROR_TEMP);
        statistics.update(Statistics.RESPONSE_ERROR_TEMP);
        statistics.update(Statistics.RESPONSE_CODE_SUCCESS);
        statistics.update(Statistics.RESPONSE_ERROR_TEMP);
        statistics.update(Statistics.RESPONSE_ERROR_PERMANENT);
        statistics.update(0);
        statistics.update(0);

        assertEquals(statistics.getSuccessfulSendedEmails(), 3);
        assertEquals(statistics.getPermanentErrors(), 1);
        assertEquals(statistics.getTemporarilyErrors(), 4);
        assertEquals(statistics.getProcessedEmails(), 8);
        assertEquals(statistics.getNotProcessedEmails(), 2);
    }

    @Test
    public void print() throws Exception {
    }

}