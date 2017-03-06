package net.lr.tinyhttp;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;

import net.lr.tinyhttp.Handler;
import net.lr.tinyhttp.HttpRequest;
import net.lr.tinyhttp.HttpResponse;
import net.lr.tinyhttp.HTTPStatus;

public class MyServlet implements Handler {

    private static final Charset CHARSET = Charset.forName("utf-8");

    @Override
    public void process(String alias, HttpRequest request, HttpResponse response) throws IOException {
        String msg = "test";
        response.status(HTTPStatus.OK, "OK");
        response.addHeader(Headers.CONTENT_TYPE, "text/html; charset=utf-8");
        response.addHeader(Headers.CONTENT_LENGTH, new Long(msg.getBytes(CHARSET).length).toString());
        response.addHeader(Headers.CONNECTION, Headers.VALUE_KEEP_ALIVE);
        response.writeHeaders();
        BufferedWriter writer = response.getWriter(CHARSET);
        writer.write(msg);
        writer.flush();
    }

}
