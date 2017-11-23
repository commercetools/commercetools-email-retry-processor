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
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.*;


public class EmailProcessorTest {


    public EmailProcessor emailProcessor = null;
    TenantConfiguration tenantConfiguration = null;
    List<CustomObject<JsonNode>> customObjects = null;
    private static final Logger LOG = LoggerFactory.getLogger(EmailProcessor.class);

    @Before
    public void setUp() throws Exception {
        customObjects = new ArrayList<CustomObject<JsonNode>>();
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
        Statistics statistic = emailProcessor.processEmails(tenantConfiguration).toCompletableFuture().join();
        assertEquals(statistic.getProcessedEmails(), 3);
        assertEquals(statistic.getSuccessfulSendedEmails(), 1);
        assertEquals(statistic.getTemporarilyErrors(), 1);
        assertEquals(statistic.getPermanentErrors(), 1);

        customObjects = new ArrayList<CustomObject<JsonNode>>();
        customObjects.add(createCustomObject("1", "pending", Statistics.RESPONSE_CODE_SUCCESS));
        customObjects.add(createCustomObject("2", "pending", Statistics.RESPONSE_ERROR_TEMP));
        customObjects.add(createCustomObject("3", "pending", Statistics.RESPONSE_ERROR_TEMP));
        customObjects.add(createCustomObject("4", "pending", Statistics.RESPONSE_ERROR_PERMANENT));
        tenantConfiguration.setClient(mockSphereClient(customObjects));
        statistic = emailProcessor.processEmails(tenantConfiguration).toCompletableFuture().join();
        assertEquals(statistic.getProcessedEmails(), 4);
        assertEquals(statistic.getSuccessfulSendedEmails(), 1);
        assertEquals(statistic.getTemporarilyErrors(), 2);
        assertEquals(statistic.getPermanentErrors(), 1);

        customObjects = new ArrayList<CustomObject<JsonNode>>();
        customObjects.add(createCustomObject("1", "pending", Statistics.RESPONSE_CODE_SUCCESS));
        customObjects.add(createCustomObject("2", "pending", Statistics.RESPONSE_CODE_SUCCESS));
        customObjects.add(createCustomObject("3", "pending", Statistics.RESPONSE_CODE_SUCCESS));
        tenantConfiguration.setClient(mockSphereClient(customObjects));
        statistic = emailProcessor.processEmails(tenantConfiguration).toCompletableFuture().join();
        assertEquals(statistic.getProcessedEmails(), 3);
        assertEquals(statistic.getSuccessfulSendedEmails(), 3);
        assertEquals(statistic.getTemporarilyErrors(), 0);
        assertEquals(statistic.getPermanentErrors(), 0);
    }


    @Test
    public void shouldNOTProcessEmails() throws Exception {
        customObjects.add(createCustomObject("1", "error", Statistics.RESPONSE_CODE_SUCCESS));
        customObjects.add(createCustomObject("2", "error", Statistics.RESPONSE_ERROR_TEMP));
        customObjects.add(createCustomObject("3", "error", Statistics.RESPONSE_CODE_SUCCESS));
        tenantConfiguration.setClient(mockSphereClient(customObjects));
        Statistics statistic = emailProcessor.processEmails(tenantConfiguration).toCompletableFuture().join();
        ;
        assertEquals(statistic.getProcessedEmails(), 0);
        assertEquals(statistic.getSuccessfulSendedEmails(), 0);
        assertEquals(statistic.getTemporarilyErrors(), 0);

    }

    @Test
    public void shouldNOTProcessEmails2() throws Exception {
        tenantConfiguration.setClient(mockSphereClient(Collections.emptyList()));
        Statistics statistic = emailProcessor.processEmails(tenantConfiguration).toCompletableFuture().join();

        assertEquals(statistic.getProcessedEmails(), 0);
        assertEquals(statistic.getSuccessfulSendedEmails(), 0);
        assertEquals(statistic.getTemporarilyErrors(), 0);

    }

    @Test
    public void shouldcallApiEndpoint() throws Exception {
        Mockito.doCallRealMethod().when(emailProcessor).callApiEndpoint(Mockito.anyString(), Mockito.any
                (TenantConfiguration.class));
        int httpStatus = 200;
        String id = "123";
        String tenantid = "testTenant";
        String url = "http://www.anyurl.de";
        MockURLStreamHandler handler = new MockURLStreamHandler(url);
        URL.setURLStreamHandlerFactory(handler);
        MockHttpURLConnection httpURLConnection = handler.getConnection();
        TenantConfiguration configuration = new TenantConfiguration();
        configuration.setHttpURLConnection(httpURLConnection);
        configuration.setProjectKey(tenantid);
        int result = emailProcessor.callApiEndpoint(id, configuration);
        assertEquals(httpStatus, result);
        assertEquals(url, httpURLConnection.getURL().toString());
        assertEquals(IOUtils.toString(httpURLConnection.getInputStream()), "emailid=" + id + "&tenantid=" + tenantid);

    }


    private SphereClient mockSphereClient(List<CustomObject<JsonNode>> customObjects) {
        SphereClient client = Mockito.mock(SphereClient.class);
        PagedQueryResult queryResult = Mockito.mock(PagedQueryResult.class);
        Mockito.when(queryResult.getTotal()).thenReturn(Long.valueOf(customObjects.size()));
        Mockito.when(queryResult.getResults()).thenReturn(customObjects);
        Mockito.when(client.execute(Mockito.isA(CustomObjectQuery.class))).thenReturn(CompletableFuture
                .completedFuture(queryResult));
        return client;
    }


    private CustomObject createCustomObject(String customObjectID, String status, int webHookhttpStatus) throws Exception {
        Mockito.when(emailProcessor.callApiEndpoint(customObjectID, tenantConfiguration)).thenReturn(webHookhttpStatus);
        JsonNode jsonNode = SphereJsonUtils.parse(String.format("{\"status\":\"%s\"}", status));
        CustomObject customObject = Mockito.mock(CustomObject.class);
        Mockito.when(customObject.getId()).thenReturn(customObjectID);
        Mockito.when(customObject.getValue()).thenReturn(jsonNode);
        return customObject;
    }

    public class MockURLStreamHandler extends URLStreamHandler implements URLStreamHandlerFactory {
        private MockHttpURLConnection mConnection;

        public MockHttpURLConnection getConnection() {
            return mConnection;
        }

        // *** URLStreamHandler

        @Override
        public HttpURLConnection openConnection(URL u) throws IOException {
            mConnection = new MockHttpURLConnection(u);
            return mConnection;
        }

        // *** URLStreamHandlerFactory

        @Override
        public URLStreamHandler createURLStreamHandler(String protocol) {
            return this;
        }

        public MockURLStreamHandler(String url) throws Exception {
            openConnection(new URL(url));
        }
    }

    public class MockHttpURLConnection extends HttpURLConnection {

        protected MockHttpURLConnection(URL url) {
            super(url);
        }

        ByteArrayOutputStream stream;

        // *** HttpURLConnection
        @Override
        public int getResponseCode() throws IOException {
            return 200;

        }

        @Override
        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(stream.toByteArray());
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            stream = new ByteArrayOutputStream();
            return stream;
        }

        @Override
        public void connect() throws IOException {
        }

        @Override
        public void disconnect() {
        }

        @Override
        public boolean usingProxy() {
            return false;
        }

    }

}