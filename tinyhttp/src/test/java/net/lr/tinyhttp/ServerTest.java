package net.lr.tinyhttp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.lr.tinyhttp.impl.Server;

public class ServerTest {
    private static final int PORT = 8080;
    Logger LOG = LoggerFactory.getLogger(FileServerTest.class);
    private static Server server;
    private static CloseableHttpClient client;
    
    @BeforeClass
    public static void setupServer() {
        server = new Server();
        server.start("localhost", PORT, 10);
        server.addHandler("/testServlet", new MyServlet());
        server.addHandler("/testThreading", new DelayingHandler());
        client = HttpClientBuilder.create().build();
    }
    
    @AfterClass
    public static void shutdownServer() throws IOException {
        server.close();
    }

    @Test
    public void testGet() throws IOException {
        HttpGet request = new HttpGet("http://localhost:" + PORT + "/index.html");
        request.setProtocolVersion(HttpVersion.HTTP_1_1);
        CloseableHttpResponse response = client.execute(request);
        String content = getContent(response);
        Assert.assertEquals("", content);
    }
    
    @Test
    public void testPostMethod() throws IOException {
        HttpPost request = new HttpPost("http://localhost:" + PORT + "/testServlet");
        request.setProtocolVersion(HttpVersion.HTTP_1_1);
        CloseableHttpResponse response = client.execute(request);
        String content = getContent(response);
        Assert.assertEquals("", content);
    }

    
    @Test
    public void testGetPerformance() throws IOException {
        HttpGet request = new HttpGet("http://localhost:" + PORT + "/testServlet");
        request.setProtocolVersion(HttpVersion.HTTP_1_1);
        StopWatch watch = StopWatch.createStarted();
        for (int c = 0; c < 10000; c++) {
            CloseableHttpResponse response = client.execute(request);
            InputStream in = response.getEntity().getContent();
            in.close();
            if (c%100==0) {
                System.out.println(c);
            }
        }
        server.close();
        Assert.assertTrue(watch.getTime(TimeUnit.SECONDS) < 5);
        
    }
    
    @Test
    public void testThreading() throws IOException {
        

        assertWeHaveDelay(client);

        StopWatch watch = StopWatch.createStarted();
        ExecutorService executor = Executors.newFixedThreadPool(10);
        for (int c=0; c<10; c++) {
            executor.execute(() -> doGet(client, "/test"));
        }
        Assert.assertTrue(watch.getTime(TimeUnit.MILLISECONDS) < 1100);
    }
    
    @Test
    public void testNotFound() throws IOException {
        client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet("http://localhost:" + PORT + "/invalid");
        request.setProtocolVersion(HttpVersion.HTTP_1_1);
        
        try (CloseableHttpResponse response = client.execute(request)) {
            Assert.assertEquals(404, response.getStatusLine().getStatusCode());
        }
    }

    private void assertWeHaveDelay(CloseableHttpClient client) {
        StopWatch watch = StopWatch.createStarted();
        doGet(client, "/testThreading");
        long duration = watch.getTime(TimeUnit.MILLISECONDS);
        Assert.assertTrue("Duration too short " + duration, duration > 900);
    }
    
    private void doGet(CloseableHttpClient client, String path) {
        HttpGet request = new HttpGet("http://localhost:" + PORT + path);
        request.setProtocolVersion(HttpVersion.HTTP_1_1);
        
        try (CloseableHttpResponse response = client.execute(request)) {
            String result = getContent(response);            
            System.out.println(result);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private String getContent(CloseableHttpResponse response) throws IOException {
        InputStream in = response.getEntity().getContent();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, Charset.forName("UTF-8")));
        return reader.lines().collect(Collectors.joining());
    }
}
