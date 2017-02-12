package net.lr.tinyhttp;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;

import net.lr.tinyhttp.Handler;
import net.lr.tinyhttp.HttpRequest;
import net.lr.tinyhttp.HttpResponse;
import net.lr.tinyhttp.HTTPStatus;

public class MyServlet implements Handler {

    @Override
    public void process(String alias, HttpRequest request, HttpResponse response) throws IOException {
        response.writeStatus(HTTPStatus.OK, "OK");
        response.writeHeader("Content-Type", "text/html; charset=utf-8\n");
        response.endHeaders();
        BufferedWriter writer = response.getWriter(Charset.forName("utf-8"));
        writer.write("test\n");
    }

}
