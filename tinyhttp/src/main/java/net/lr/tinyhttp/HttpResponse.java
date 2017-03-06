package net.lr.tinyhttp;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class HttpResponse {
    private OutputStream out;
    private Writer writer;
    private ProtocolVersion protocolVersion;
    private String statusLine;
    private Map<String, String> headers;
    
    public HttpResponse(ProtocolVersion protocolVersion, OutputStream out) {
        this.protocolVersion = protocolVersion;
        this.out = out;
        this.writer = new OutputStreamWriter(out);
        this.headers = new HashMap<>();
        
    }
    
    public void status(int status, String message) throws IOException {
        this.statusLine = protocolVersion + " " + status + " " + message;
    }

    public void addHeader(String key, String value) throws IOException {
        headers.put(key, value);

    }
    
    public void writeHeaders() throws IOException {
        writer.write(statusLine + "\r\n");
        for (String key : headers.keySet()) {
            writer.write(key + ": " + headers.get(key) + "\r\n");
        }
        writer.write("\r\n");
        writer.flush();
    }
    
    public OutputStream getOutputStream() {
        return this.out;
    }

    public BufferedWriter getWriter(Charset charset) {
        return new BufferedWriter(new OutputStreamWriter(out, charset));
    }
    
    public void methodNotAllowed() throws IOException {
        status(HTTPStatus.METHOD_NOT_ALLOWED, "Not allowed");
        writeHeaders();
    }
    
    public void notFound() throws IOException {
        status(HTTPStatus.NOT_FOUND, "Not found");
        writeHeaders();
    }

    public void forbidden() throws IOException {
        status(HTTPStatus.FORBIDDEN, "Forbidden");
        writeHeaders();
    }

    public String getHeader(String key) {
        return this.headers.get(key);
    }
}
