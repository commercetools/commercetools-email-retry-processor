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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.commercetools.emailprocessor.email.EmailProcessor.EMAIL_STATUS_ERROR;
import static com.commercetools.emailprocessor.email.EmailProcessor.EMAIL_STATUS_PENDING;
import static org.junit.Assert.assertEquals;
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
        tenantConfiguration = new TenantConfiguration();
        tenantConfiguration.setProjectKey("testproject");
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
    public void processEmail_NoPendingEmailAvailable_shouldNotProcessEmails() throws Exception {
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
    public void  callApiEndpoint_validEndpointUrlisGiven_shouldAddCorrectVariablesToRequest()  throws Exception {
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
        assertEquals(IOUtils.toString(httpUrlConnection.getInputStream(), Charset.defaultCharset()),
                "emailid=" + id + "&tenantid=" + tenantid);

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
            endPointtatus)
            throws Exception {
        when(emailProcessor.callApiEndpoint(customobjectid, tenantConfiguration)).thenReturn(endPointtatus);
        JsonNode jsonNode = SphereJsonUtils.parse(String.format("{\"status\":\"%s\"}", status));
        CustomObject<JsonNode> customObject = mock(CustomObject.class);
        when(customObject.getId()).thenReturn(customobjectid);
        when(customObject.getValue()).thenReturn(jsonNode);
        return customObject;
    }

    public static class MockUrlStreamHandler extends URLStreamHandler implements URLStreamHandlerFactory {
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

    public static class MockHttpUrlConnection extends HttpURLConnection {

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