package net.lr.tinyhttp;

import java.io.IOException;

import org.apache.http.HttpStatus;

public class DelayingHandler implements Handler {

    @Override
    public void process(String alias, HttpRequest request, HttpResponse response) throws IOException {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        response.writeStatus(HttpStatus.SC_OK, "OK");
        response.endHeaders();

    }

}
