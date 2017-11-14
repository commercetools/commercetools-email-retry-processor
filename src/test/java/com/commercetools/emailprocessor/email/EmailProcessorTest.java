package com.commercetools.emailprocessor.email;

import com.commercetools.emailprocessor.model.Email;
import com.commercetools.emailprocessor.model.Statistics;
import com.commercetools.emailprocessor.model.TenantConfiguration;
import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.customobjects.CustomObject;
import io.sphere.sdk.customobjects.queries.CustomObjectQuery;
import io.sphere.sdk.queries.PagedQueryResult;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.*;

public class EmailProcessorTest {


    public EmailProcessor emailProcessor = null;
    TenantConfiguration tenantConfiguration = null;
    List<CustomObject<Email>> customObjects = null;
    private static final Logger LOG = LoggerFactory.getLogger(EmailProcessor.class);

    @Before
    public void setUp() throws Exception {
        customObjects = new ArrayList<CustomObject<Email>>();
        emailProcessor = Mockito.mock(EmailProcessor.class);
        Mockito.doCallRealMethod().when(emailProcessor).processEmails(Mockito.any(TenantConfiguration.class));
        tenantConfiguration = new TenantConfiguration();
        tenantConfiguration.setProjectKey("testproject");
        //tenantConfiguration.setClient(mockSphereClient());
    }

    @Test
    public void shouldProcessEmails() throws Exception {
        customObjects.add(createCustomObject("1", "pending", Statistics.RESPONSE_CODE_SUCCESS));
        customObjects.add(createCustomObject("2", "pending", Statistics.RESPONSE_ERROR_TEMP));
        customObjects.add(createCustomObject("3", "error", Statistics.RESPONSE_ERROR_TEMP));
        customObjects.add(createCustomObject("4", "pending", Statistics.RESPONSE_ERROR_PERMANENT));
        tenantConfiguration.setClient(mockSphereClient(customObjects));
        Statistics statistic = emailProcessor.processEmails(tenantConfiguration);
        assertEquals(statistic.getProcessedEmail(), 3);
        assertEquals(statistic.getSuccessful(), 1);
        assertEquals(statistic.getTempError(), 1);
        assertEquals(statistic.getPermanentError(), 1);
    }
    @Test
    public void shouldProcessEmails2() throws Exception {
        customObjects.add(createCustomObject("1", "pending", Statistics.RESPONSE_CODE_SUCCESS));
        customObjects.add(createCustomObject("2", "pending", Statistics.RESPONSE_ERROR_TEMP));
        customObjects.add(createCustomObject("3", "pending", Statistics.RESPONSE_ERROR_TEMP));
        customObjects.add(createCustomObject("4", "pending", Statistics.RESPONSE_ERROR_PERMANENT));
        tenantConfiguration.setClient(mockSphereClient(customObjects));
        Statistics statistic = emailProcessor.processEmails(tenantConfiguration);
        assertEquals(statistic.getProcessedEmail(), 4);
        assertEquals(statistic.getSuccessful(), 1);
        assertEquals(statistic.getTempError(), 2);
        assertEquals(statistic.getPermanentError(), 1);
    }


    @Test
    public void shouldProcessEmails_all_successfull() throws Exception {
        customObjects.add(createCustomObject("1", "pending", Statistics.RESPONSE_CODE_SUCCESS));
        customObjects.add(createCustomObject("2", "pending", Statistics.RESPONSE_CODE_SUCCESS));
        customObjects.add(createCustomObject("3", "pending", Statistics.RESPONSE_CODE_SUCCESS));
        tenantConfiguration.setClient(mockSphereClient(customObjects));
        Statistics statistic = emailProcessor.processEmails(tenantConfiguration);
        assertEquals(statistic.getProcessedEmail(), 3);
        assertEquals(statistic.getSuccessful(), 3);
        assertEquals(statistic.getTempError(), 0);
        assertEquals(statistic.getPermanentError(), 0);
    }


    @Test
    public void shouldNOTProcessEmails() throws Exception {
        customObjects.add(createCustomObject("1", "error", Statistics.RESPONSE_CODE_SUCCESS));
        customObjects.add(createCustomObject("2", "error", Statistics.RESPONSE_ERROR_TEMP));
        customObjects.add(createCustomObject("3", "error", Statistics.RESPONSE_CODE_SUCCESS));
        tenantConfiguration.setClient(mockSphereClient(customObjects));
        Statistics statistic = emailProcessor.processEmails(tenantConfiguration);
        assertEquals(statistic.getProcessedEmail(), 0);
        assertEquals(statistic.getSuccessful(), 0);
        assertEquals(statistic.getTempError(), 0);

    }

    @Test
    public void shouldNOTProcessEmails2() throws Exception {
        tenantConfiguration.setClient(mockSphereClient(Collections.emptyList()));
        Statistics statistic = emailProcessor.processEmails(tenantConfiguration);
        assertEquals(statistic.getProcessedEmail(), 0);
        assertEquals(statistic.getSuccessful(), 0);
        assertEquals(statistic.getTempError(), 0);

    }

    public void shouldCallWebhook() throws Exception {
        tenantConfiguration.setClient(mockSphereClient(Collections.emptyList()));
        Statistics statistic = emailProcessor.processEmails(tenantConfiguration);
        assertEquals(statistic.getProcessedEmail(), 0);
        assertEquals(statistic.getSuccessful(), 0);
        assertEquals(statistic.getTempError(), 0);

    }


    SphereClient mockSphereClient(List<CustomObject<Email>> customObjects) {
        SphereClient client = Mockito.mock(SphereClient.class);
        PagedQueryResult<CustomObject<Email>> queryResult = Mockito.mock(PagedQueryResult.class);
        Mockito.when(queryResult.getTotal()).thenReturn(Long.valueOf(customObjects.size()));
        Mockito.when(queryResult.getResults()).thenReturn(customObjects);
        Mockito.when(client.execute(Mockito.isA(CustomObjectQuery.class))).thenReturn(CompletableFuture
                .completedFuture(queryResult));
        return client;
    }


    private CustomObject<Email> createCustomObject(String customObjectID, String status, int webHookhttpStatus) {
        Mockito.when(emailProcessor.callWebHook(customObjectID, tenantConfiguration)).thenReturn(webHookhttpStatus);
        Email email=new Email();
        email.setStatus(status);
        CustomObject<Email> customObject=Mockito.mock(CustomObject.class);
        Mockito.when(customObject.getId()).thenReturn(customObjectID);
        Mockito.when(customObject.getValue()).thenReturn(email);


        return customObject;
    }

    public static String stringFromResource(final String resourcePath) throws Exception {
        return IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream(
                resourcePath), "UTF-8");
    }
}