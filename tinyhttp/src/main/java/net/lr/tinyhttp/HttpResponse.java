package net.lr.tinyhttp;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

public class HttpResponse {
    private OutputStream out;
    private Writer writer;
    private ProtocolVersion protocolVersion;
    
    public HttpResponse(ProtocolVersion protocolVersion, OutputStream out) {
        this.protocolVersion = protocolVersion;
        this.out = out;
        this.writer = new OutputStreamWriter(out);
    }
    
    public void writeStatus(int status, String message) throws IOException {
        writer.write(protocolVersion + " " + status + " " + message + "\r\n");
    }

    public void writeHeader(String key, String value) throws IOException {
        writer.write(key + ": " + value + "\r\n");
    }
    
    public void endHeaders() throws IOException {
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
        writeStatus(HTTPStatus.METHOD_NOT_ALLOWED, "Not allowed");
        endHeaders();
    }
    
    public void notFound() throws IOException {
        writeStatus(HTTPStatus.NOT_FOUND, "Not found");
        endHeaders();
    }

    public void forbidden() throws IOException {
        writeStatus(HTTPStatus.FORBIDDEN, "Forbidden");
        endHeaders();
    }
}
