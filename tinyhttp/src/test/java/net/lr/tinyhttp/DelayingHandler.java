package net.lr.tinyhttp;

import java.io.IOException;

public class DelayingHandler implements Handler {

    @Override
    public void process(String alias, HttpRequest request, HttpResponse response) throws IOException {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        response.status(HTTPStatus.OK, "OK");
        response.addHeader(Headers.CONTENT_LENGTH, new Long(0).toString());
        response.addHeader(Headers.CONNECTION, Headers.VALUE_CLOSE);
        response.writeHeaders();

    }

}
