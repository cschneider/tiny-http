package net.lr.tinyhttp.itest;

import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.cm.ConfigurationAdminOptions.newConfiguration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.stream.Collectors;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExamServer;

import net.lr.tinyhttp.HTTPStatus;

public class ServerTest {
    private static final int PORT = 8080;

    @Rule
    public PaxExamServer server = new PaxExamServer();

    @Configuration
    public static Option[] configure() {
        return new Option[] //
        {
         mavenBundle("org.ops4j.pax.logging", "pax-logging-api").versionAsInProject(),
         mavenBundle("org.ops4j.pax.logging", "pax-logging-service").versionAsInProject(),
         mavenBundle("org.apache.felix", "org.apache.felix.configadmin").versionAsInProject(),
         mavenBundle("org.apache.felix", "org.apache.felix.scr").versionAsInProject(),
         mavenBundle("net.lr.tinyhttp", "net.lr.tinyhttp.server").versionAsInProject(),
         configureFileHandler()
        };
    }

    private static Option configureFileHandler() {
        return newConfiguration("tinyhttp.handler.file") //
             .put("fileBase", "src/test/resources/web") //
             .asOption();
    }

    @Test
    public void testGetHtml() throws IOException, InterruptedException {
        CloseableHttpClient client = HttpClientBuilder.create().build();
        HttpGet get = new HttpGet("http://localhost:" + PORT + "/index.html");
        CloseableHttpResponse response = client.execute(get);
        Assert.assertEquals(HTTPStatus.OK, response.getStatusLine().getStatusCode());
        Assert.assertEquals("text/html", response.getFirstHeader("Content-type").getValue());
        String content = getContent(response);
        Assert.assertTrue(content.contains("Testpage"));
    }

    private String getContent(CloseableHttpResponse response) throws IOException {
        InputStream in = response.getEntity().getContent();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, Charset.forName("UTF-8")));
        return reader.lines().collect(Collectors.joining());
    }

}
