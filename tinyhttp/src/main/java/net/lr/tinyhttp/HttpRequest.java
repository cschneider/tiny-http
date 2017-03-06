package net.lr.tinyhttp;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class HttpRequest implements Closeable {
    private String method;
    private String path;
    private ProtocolVersion protocolVersion;
    private Map<String, String> arguments;
    private Map<String, String> headers;
    private BufferedReader reader;
    
    public HttpRequest(InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, Charset.forName("UTF-8")));
        this.reader = reader;
        parseCommand(reader.readLine());
        String line = reader.readLine();
        Map<String, String> headers = new HashMap<>();
        while (line != null && !"".equals(line)) {
            StringTokenizer tokenizer = new StringTokenizer(line, ": ");
            headers.put(tokenizer.nextToken(), tokenizer.nextToken());
            line = reader.readLine();
        }
        this.headers = headers;
    }

    private void parseCommand(String line) {
        if (line == null) {
            this.method = "";
            this.path = "";
            this.protocolVersion = new ProtocolVersion("HTTP", "1.1");
            return;
        }
        StringTokenizer tokenizer = new StringTokenizer(line, " ");
        this.method = tokenizer.nextToken();
        String fullpath = tokenizer.nextToken();
        this.path = fullpath;
        String protVersion = tokenizer.nextToken();
        StringTokenizer protTokenizer = new StringTokenizer(protVersion, "/");
        this.protocolVersion = new ProtocolVersion(protTokenizer.nextToken(), protTokenizer.nextToken());
    }
    
    public HTTPMethod getMethod() {
        try {
            return HTTPMethod.valueOf(method);
        } catch (IllegalArgumentException e) {
            throw new MethodNotAllowedException(method);
        }
    }
    
    public String getMethodSt() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public ProtocolVersion getProtocolVersion() {
        return protocolVersion;
    }

    public Map<String, String> getArguments() {
        return arguments;
    }

    public String getHeader(String key) {
        return this.headers.get(key);
    }

    @Override
    public void close() throws IOException {
        this.reader.close();
    }
    
    
}
