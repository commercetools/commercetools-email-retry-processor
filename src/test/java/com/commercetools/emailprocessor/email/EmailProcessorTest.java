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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;


public class EmailProcessorTest {


    private static final Logger LOG = LoggerFactory.getLogger(EmailProcessor.class);
    public EmailProcessor emailProcessor = null;
    TenantConfiguration tenantConfiguration = null;
    List<CustomObject<JsonNode>> customObjects = null;

    /**
     * Setup a email processor mock.
     *
     * @throws Exception when the mock cannot created
     */
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
    public void shouldNotProcessEmails() throws Exception {
        customObjects.add(createCustomObject("1", "error", Statistics.RESPONSE_CODE_SUCCESS));
        customObjects.add(createCustomObject("2", "error", Statistics.RESPONSE_ERROR_TEMP));
        customObjects.add(createCustomObject("3", "error", Statistics.RESPONSE_CODE_SUCCESS));
        tenantConfiguration.setClient(mockSphereClient(customObjects));
        Statistics statistic = emailProcessor.processEmails(tenantConfiguration).toCompletableFuture().join();
        assertEquals(statistic.getProcessedEmails(), 0);
        assertEquals(statistic.getSuccessfulSendedEmails(), 0);
        assertEquals(statistic.getTemporarilyErrors(), 0);

    }

    @Test
    public void shouldNotProcessEmails2() throws Exception {
        tenantConfiguration.setClient(mockSphereClient(Collections.emptyList()));
        Statistics statistic = emailProcessor.processEmails(tenantConfiguration).toCompletableFuture().join();

        assertEquals(statistic.getProcessedEmails(), 0);
        assertEquals(statistic.getSuccessfulSendedEmails(), 0);
        assertEquals(statistic.getTemporarilyErrors(), 0);

    }

    @Test
    public void shouldcallApiEndpoint() throws Exception {
        Mockito.doCallRealMethod().when(emailProcessor)
            .callApiEndpoint(Mockito.anyString(), Mockito.any(TenantConfiguration.class));
        final int httpStatus = 200;
        final String id = "123";
        final String tenantid = "testTenant";
        final String url = "http://www.anyurl.de";
        MockUrlStreamHandler handler = new MockUrlStreamHandler(url);
        URL.setURLStreamHandlerFactory(handler);
        MockHttpUrlConnection httpUrlConnection = handler.getConnection();
        TenantConfiguration configuration = new TenantConfiguration();
        configuration.setHttpUrlConnection(httpUrlConnection);
        configuration.setProjectKey(tenantid);
        int result = emailProcessor.callApiEndpoint(id, configuration);
        assertEquals(httpStatus, result);
        assertEquals(url, httpUrlConnection.getURL().toString());
        assertEquals(IOUtils.toString(httpUrlConnection.getInputStream(), Charset.defaultCharset()), "emailid=" + id + "&tenantid=" + tenantid);

    }


    private SphereClient mockSphereClient(final List<CustomObject<JsonNode>> customObjects) {
        SphereClient client = Mockito.mock(SphereClient.class);
        PagedQueryResult queryResult = Mockito.mock(PagedQueryResult.class);
        Mockito.when(queryResult.getTotal()).thenReturn(Long.valueOf(customObjects.size()));
        Mockito.when(queryResult.getResults()).thenReturn(customObjects);
        Mockito.when(client.execute(Mockito.isA(CustomObjectQuery.class))).thenReturn(CompletableFuture
            .completedFuture(queryResult));
        return client;
    }


    private CustomObject createCustomObject(final String customobjectid, final String status, final int endPointtatus)
        throws Exception {
        Mockito.when(emailProcessor.callApiEndpoint(customobjectid, tenantConfiguration)).thenReturn(endPointtatus);
        JsonNode jsonNode = SphereJsonUtils.parse(String.format("{\"status\":\"%s\"}", status));
        CustomObject customObject = Mockito.mock(CustomObject.class);
        Mockito.when(customObject.getId()).thenReturn(customobjectid);
        Mockito.when(customObject.getValue()).thenReturn(jsonNode);
        return customObject;
    }

    public class MockUrlStreamHandler extends URLStreamHandler implements URLStreamHandlerFactory {
        private MockHttpUrlConnection mockConnection;

        public MockUrlStreamHandler(final String url) throws Exception {
            openConnection(new URL(url));
        }

        // *** URLStreamHandler

        public MockHttpUrlConnection getConnection() {
            return mockConnection;
        }

        // *** URLStreamHandlerFactory

        @Override
        public HttpURLConnection openConnection(final URL url) throws IOException {
            mockConnection = new MockHttpUrlConnection(url);
            return mockConnection;
        }

        @Override
        public URLStreamHandler createURLStreamHandler(final String protocol) {
            return this;
        }
    }

    public class MockHttpUrlConnection extends HttpURLConnection {

        ByteArrayOutputStream stream;

        protected MockHttpUrlConnection(final URL url) {
            super(url);
        }

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