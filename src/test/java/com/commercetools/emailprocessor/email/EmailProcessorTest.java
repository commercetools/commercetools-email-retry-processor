package com.commercetools.emailprocessor.email;

import com.commercetools.emailprocessor.model.Statistics;
import com.commercetools.emailprocessor.model.TenantConfiguration;
import com.fasterxml.jackson.databind.JsonNode;
import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.customobjects.CustomObject;
import io.sphere.sdk.customobjects.queries.CustomObjectQuery;
import io.sphere.sdk.json.SphereJsonUtils;
import io.sphere.sdk.queries.PagedQueryResult;
import org.apache.commons.io.IOUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.commercetools.emailprocessor.email.EmailProcessor.EMAIL_STATUS_ERROR;
import static com.commercetools.emailprocessor.email.EmailProcessor.EMAIL_STATUS_PENDING;
import static javax.crypto.Cipher.ENCRYPT_MODE;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class EmailProcessorTest {


    private EmailProcessor emailProcessor;
    private TenantConfiguration tenantConfiguration;
    private List<CustomObject<JsonNode>> customObjects;

    /**
     * Setup a email processor mock.
     *
     * @throws Exception when the mock cannot created
     */
    @Before
    public void setUp() throws Exception {
        customObjects = new ArrayList<>();
        emailProcessor = mock(EmailProcessor.class);
        Mockito.doCallRealMethod().when(emailProcessor).processEmails(Mockito.any(TenantConfiguration.class));
        Mockito.doCallRealMethod().when(emailProcessor).blowFish(anyString(), anyString(), anyInt());
        tenantConfiguration = new TenantConfiguration();
        tenantConfiguration.setProjectKey("testproject");
        tenantConfiguration.setEncryptionKey("1234567899053146");
    }

    @Test
    public void processEmail_pendingEmailAvailable_shouldProcessEmails() throws Exception {
        customObjects.add(createCustomObject("1", EMAIL_STATUS_PENDING, Statistics.RESPONSE_CODE_SUCCESS));
        customObjects.add(createCustomObject("2", EMAIL_STATUS_PENDING, Statistics.RESPONSE_ERROR_TEMP));
        customObjects.add(createCustomObject("3", EMAIL_STATUS_ERROR, Statistics.RESPONSE_ERROR_TEMP));
        customObjects.add(createCustomObject("4", EMAIL_STATUS_PENDING, Statistics.RESPONSE_ERROR_PERMANENT));
        tenantConfiguration.setClient(mockSphereClient(customObjects));
        Statistics statistic = emailProcessor.processEmails(tenantConfiguration).toCompletableFuture().join();
        assertEquals(statistic.getProcessed(), 3);
        assertEquals(statistic.getSentSuccessfully(), 1);
        assertEquals(statistic.getTemporarilyErrors(), 1);
        assertEquals(statistic.getPermanentErrors(), 1);

        customObjects = new ArrayList<CustomObject<JsonNode>>();
        customObjects.add(createCustomObject("1", EMAIL_STATUS_PENDING, Statistics.RESPONSE_CODE_SUCCESS));
        customObjects.add(createCustomObject("2", EMAIL_STATUS_PENDING, Statistics.RESPONSE_ERROR_TEMP));
        customObjects.add(createCustomObject("3", EMAIL_STATUS_PENDING, Statistics.RESPONSE_ERROR_TEMP));
        customObjects.add(createCustomObject("4", EMAIL_STATUS_PENDING, Statistics.RESPONSE_ERROR_PERMANENT));
        tenantConfiguration.setClient(mockSphereClient(customObjects));
        statistic = emailProcessor.processEmails(tenantConfiguration).toCompletableFuture().join();
        assertEquals(statistic.getProcessed(), 4);
        assertEquals(statistic.getSentSuccessfully(), 1);
        assertEquals(statistic.getTemporarilyErrors(), 2);
        assertEquals(statistic.getPermanentErrors(), 1);

        customObjects = new ArrayList<CustomObject<JsonNode>>();
        customObjects.add(createCustomObject("1", EMAIL_STATUS_PENDING, Statistics.RESPONSE_CODE_SUCCESS));
        customObjects.add(createCustomObject("2", EMAIL_STATUS_PENDING, Statistics.RESPONSE_CODE_SUCCESS));
        customObjects.add(createCustomObject("3", EMAIL_STATUS_PENDING, Statistics.RESPONSE_CODE_SUCCESS));
        tenantConfiguration.setClient(mockSphereClient(customObjects));
        statistic = emailProcessor.processEmails(tenantConfiguration).toCompletableFuture().join();
        assertEquals(statistic.getProcessed(), 3);
        assertEquals(statistic.getSentSuccessfully(), 3);
        assertEquals(statistic.getTemporarilyErrors(), 0);
        assertEquals(statistic.getPermanentErrors(), 0);
    }

    @Test
    public void processEmail_processAllFlagIsSet_shouldProcessAllEmails() throws Exception {
        customObjects.add(createCustomObject("1", EMAIL_STATUS_PENDING, Statistics.RESPONSE_CODE_SUCCESS));
        customObjects.add(createCustomObject("2", EMAIL_STATUS_PENDING, Statistics.RESPONSE_ERROR_TEMP));
        customObjects.add(createCustomObject("3", EMAIL_STATUS_ERROR, Statistics.RESPONSE_ERROR_TEMP));
        customObjects.add(createCustomObject("4", EMAIL_STATUS_PENDING, Statistics.RESPONSE_ERROR_PERMANENT));
        tenantConfiguration.setClient(mockSphereClient(customObjects));
        tenantConfiguration.setProcessAll(true);
        Statistics statistic = emailProcessor.processEmails(tenantConfiguration).toCompletableFuture().join();
        assertEquals(statistic.getProcessed(), 4);
        assertEquals(statistic.getSentSuccessfully(), 1);
        assertEquals(statistic.getTemporarilyErrors(), 2);
        assertEquals(statistic.getPermanentErrors(), 1);
    }

    @Test
    public void processEmail_noPendingEmailAvailable_shouldNotProcessEmails() throws Exception {
        customObjects.add(createCustomObject("1", EMAIL_STATUS_ERROR, Statistics.RESPONSE_CODE_SUCCESS));
        customObjects.add(createCustomObject("2", EMAIL_STATUS_ERROR, Statistics.RESPONSE_ERROR_TEMP));
        customObjects.add(createCustomObject("3", EMAIL_STATUS_ERROR, Statistics.RESPONSE_CODE_SUCCESS));
        tenantConfiguration.setClient(mockSphereClient(customObjects));
        Statistics statistic = emailProcessor.processEmails(tenantConfiguration).toCompletableFuture().join();
        assertEquals(statistic.getProcessed(), 0);
        assertEquals(statistic.getSentSuccessfully(), 0);
        assertEquals(statistic.getTemporarilyErrors(), 0);

        tenantConfiguration.setClient(mockSphereClient(Collections.emptyList()));
        statistic = emailProcessor.processEmails(tenantConfiguration).toCompletableFuture().join();
        assertEquals(statistic.getProcessed(), 0);
        assertEquals(statistic.getSentSuccessfully(), 0);
        assertEquals(statistic.getTemporarilyErrors(), 0);
    }

    @Test
    public void callApiEndpoint_validEndpointUrlIsGiven_shouldAddCorrectVariablesToRequest() throws Exception {
        Mockito.doCallRealMethod().when(emailProcessor)
            .callApiEndpoint(Mockito.anyString(), Mockito.any(TenantConfiguration.class));
        final int httpStatus = Statistics.RESPONSE_CODE_SUCCESS;
        final String id = "123";
        final String tenantId = "testTenant";
        final String url = "https://httpbin.org/status/" + Statistics.RESPONSE_CODE_SUCCESS;
        final HttpPost httpPost = new HttpPost(url);

        final TenantConfiguration configuration = mock(TenantConfiguration.class);
        when(configuration.getHttpPost()).thenReturn(httpPost);
        when(configuration.getProjectKey()).thenReturn(tenantId);
        when(configuration.getEncryptionKey()).thenReturn(tenantConfiguration.getEncryptionKey());

        int result = emailProcessor.callApiEndpoint(id, configuration);
        final String encryptedEmailId = emailProcessor.blowFish(id, configuration.getEncryptionKey(), ENCRYPT_MODE);
        final List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(EmailProcessor.PARAM_EMAIL_ID, encryptedEmailId));
        params.add(new BasicNameValuePair(EmailProcessor.PARAM_TENANT_ID, configuration.getProjectKey()));
        final UrlEncodedFormEntity expectedPostEntity = new UrlEncodedFormEntity(params, Charset.defaultCharset());

        assertEquals(httpStatus, result);
        assertEquals(url, httpPost.getURI().toString());
        assertEquals(IOUtils.toString(expectedPostEntity.getContent(), Charset.defaultCharset()),
            IOUtils.toString(httpPost.getEntity().getContent(), Charset.defaultCharset()));
    }

    @SuppressWarnings("unchecked")
    private SphereClient mockSphereClient(final List<CustomObject<JsonNode>> customObjects) {
        SphereClient client = mock(SphereClient.class);
        final PagedQueryResult<CustomObject<JsonNode>> queryResult = PagedQueryResult.of(customObjects);
        when(client.execute(isA(CustomObjectQuery.class))).thenReturn(CompletableFuture.completedFuture(queryResult));
        return client;
    }

    @SuppressWarnings("unchecked")
    private CustomObject<JsonNode> createCustomObject(final String customobjectid, final String status, final int
        endPointstatus)
        throws Exception {
        when(emailProcessor.callApiEndpoint(customobjectid, tenantConfiguration)).thenReturn(endPointstatus);
        JsonNode jsonNode = SphereJsonUtils.parse(String.format("{\"status\":\"%s\"}", status));
        CustomObject<JsonNode> customObject = mock(CustomObject.class);
        when(customObject.getId()).thenReturn(customobjectid);
        when(customObject.getValue()).thenReturn(jsonNode);
        return customObject;
    }
}