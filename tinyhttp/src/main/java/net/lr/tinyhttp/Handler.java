package net.lr.tinyhttp;

import java.io.IOException;

public interface Handler {
    public static final String KEY_ALIAS = "alias";
    
    void process(String alias, HttpRequest request, HttpResponse response) throws IOException;
}
