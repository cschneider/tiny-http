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
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import net.lr.tinyhttp.impl.Server;

public class ServerTest {
    private static final String THREADING_PATH = "/testThreading";
    private static final String SERVLET_PATH = "/testServlet";
    private static final int PORT = 8080;
    private Server server;
    private CloseableHttpClient client;
    
    @Before
    public void setupServer() {
        server = new Server();
        server.start("localhost", PORT, 10, 10);
        server.addHandler(SERVLET_PATH, new MyServlet());
        server.addHandler(THREADING_PATH, new DelayingHandler());
        client = HttpClientBuilder.create().build();
    }
    
    @After
    public void shutdownServer() throws IOException {
        server.close();
    }

    @Test
    public void testGet() throws IOException {
        HttpGet request = new HttpGet("http://localhost:" + PORT + SERVLET_PATH);
        CloseableHttpResponse response = client.execute(request);
        String content = getContent(response);
        Assert.assertEquals("test", content);
    }
    
    @Test
    public void testGetPerformance() throws IOException {
        HttpGet request = new HttpGet("http://localhost:" + PORT + SERVLET_PATH);
        StopWatch watch = StopWatch.createStarted();
        for (int c = 0; c < 10000; c++) {
            CloseableHttpResponse response = client.execute(request);
            getContent(response);
            if (c%100==0) {
                System.out.println("nr " + c);
            }
        }
        server.close();
        Assert.assertTrue(watch.getTime(TimeUnit.SECONDS) < 10);
        
    }
    
    @Test
    public void testThreading() throws IOException, InterruptedException {
        assertWeHaveDelay(client);

        StopWatch watch = StopWatch.createStarted();
        ExecutorService executor = Executors.newFixedThreadPool(10);
        for (int c=0; c<10; c++) {
            executor.execute(() -> doGet(client, THREADING_PATH));
        }
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
        executor.shutdownNow();
        Assert.assertTrue(watch.getTime(TimeUnit.MILLISECONDS) < 1100);
    }
    
    @Test
    public void testNotFound() throws IOException {
        client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet("http://localhost:" + PORT + "/invalid");
        
        try (CloseableHttpResponse response = client.execute(request)) {
            Assert.assertEquals(404, response.getStatusLine().getStatusCode());
        }
    }

    private void assertWeHaveDelay(CloseableHttpClient client) {
        StopWatch watch = StopWatch.createStarted();
        doGet(client, THREADING_PATH);
        long duration = watch.getTime(TimeUnit.MILLISECONDS);
        Assert.assertTrue("Duration too short " + duration, duration > 900);
    }
    
    private void doGet(CloseableHttpClient client, String path) {
        HttpGet request = new HttpGet("http://localhost:" + PORT + path);
        
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
