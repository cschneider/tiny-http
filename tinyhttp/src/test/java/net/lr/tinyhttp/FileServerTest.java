package net.lr.tinyhttp;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.stream.Collectors;

import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.lr.tinyhttp.impl.FileHandler;
import net.lr.tinyhttp.impl.Server;

public class FileServerTest {
    private static final int PORT = 8080;
    Logger LOG = LoggerFactory.getLogger(ServerTest.class);
    private Server server;
    private CloseableHttpClient client;
    
    @Before
    public void setupServer() throws IOException {
        server = new Server();
        server.start("localhost", PORT, 10);
        server.addHandler("", new FileHandler(new File("src/test/resources/web")));
        client = HttpClientBuilder.create().build();
    }
    
    @After
    public void shutdownServer() throws IOException {
        server.close();
    }

    @Test
    public void testGetHtml() throws IOException, InterruptedException {
        CloseableHttpResponse response = client.execute(get("/index.html"));
        Assert.assertEquals(HTTPStatus.OK, response.getStatusLine().getStatusCode());
        Assert.assertEquals("text/html", response.getFirstHeader("Content-type").getValue());
        String content = getContent(response);
        Assert.assertTrue(content.contains("Testpage"));
        System.out.println(content);
    }
    
    @Test
    public void testGetIndex() throws IOException, InterruptedException {
        CloseableHttpResponse response = client.execute(get(""));
        Assert.assertEquals(HTTPStatus.OK, response.getStatusLine().getStatusCode());
        Assert.assertEquals("text/html", response.getFirstHeader("Content-type").getValue());
        String content = getContent(response);
        Assert.assertTrue(content.contains("Testpage"));
        System.out.println(content);
    }
    
    @Test
    public void testGetDirectory() throws IOException, InterruptedException {
        CloseableHttpResponse response = client.execute(get("/images"));
        Assert.assertEquals(HTTPStatus.FORBIDDEN, response.getStatusLine().getStatusCode());
    }
    
    @Test
    public void testGetImage() throws IOException, InterruptedException {
        CloseableHttpResponse response = client.execute(get("/images/wispy-cirrus-clouds-small.png"));
        Assert.assertEquals(HTTPStatus.OK, response.getStatusLine().getStatusCode());
        Assert.assertEquals("image/png", response.getFirstHeader("Content-type").getValue());
    }

    @Test
    public void testNotFound() throws IOException, InterruptedException {
        CloseableHttpResponse response = client.execute(get("/other.html"));
        getContent(response);
        Assert.assertEquals(HttpStatus.SC_NOT_FOUND, response.getStatusLine().getStatusCode());
    }
    
    @Test
    public void testAttack() throws IOException, InterruptedException {
        CloseableHttpResponse response = client.execute(get("/../log4j.properties"));
        Assert.assertEquals(HTTPStatus.FORBIDDEN, response.getStatusLine().getStatusCode());
    }
    
    private HttpGet get(String path) {
        HttpGet request = new HttpGet("http://localhost:" + PORT + path);
        request.setProtocolVersion(HttpVersion.HTTP_1_1);
        return request;
    }

    private String getContent(CloseableHttpResponse response) throws IOException {
        InputStream in = response.getEntity().getContent();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, Charset.forName("UTF-8")));
        return reader.lines().collect(Collectors.joining());
    }

}
