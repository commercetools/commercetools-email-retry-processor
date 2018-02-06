package com.commercetools.emailprocessor.model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StatisticsTest {

    private Statistics statistics = null;
    private String tenant = "anyTenant";

    @Before
    public void setUp() throws Exception {
        statistics = new Statistics();
    }

    @Test
    public void update_differentResponseCodesAreProvided_shouldShowCorrectStatistic() throws Exception {
        statistics.update(Statistics.RESPONSE_CODE_SUCCESS);
        statistics.update(Statistics.RESPONSE_CODE_SUCCESS);
        statistics.update(Statistics.RESPONSE_ERROR_TEMP);
        statistics.update(Statistics.RESPONSE_ERROR_TEMP);
        statistics.update(Statistics.RESPONSE_ERROR_TEMP);
        statistics.update(Statistics.RESPONSE_CODE_SUCCESS);
        statistics.update(Statistics.RESPONSE_ERROR_TEMP);
        statistics.update(Statistics.RESPONSE_ERROR_PERMANENT);


        assertEquals(statistics.getSentSuccessfully(), 3);
        assertEquals(statistics.getPermanentErrors(), 1);
        assertEquals(statistics.getTemporaryErrors(), 4);
        assertEquals(statistics.getProcessed(), 8);

    }

    @Test
    public void ofError_isCalledForTenant_shouldShowCorrectStatstic() throws Exception {
        statistics = Statistics.ofError(tenant);
        assertEquals(statistics.getTenantId(), tenant);
        assertEquals(statistics.getSentSuccessfully(), 0);
        assertEquals(statistics.getPermanentErrors(), 0);
        assertEquals(statistics.getTemporaryErrors(), 0);
        assertEquals(statistics.getProcessed(), 0);
        assertEquals(statistics.getGlobalError(), 1);
    }

}