package net.lr.tinyhttp.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.lr.tinyhttp.Handler;
import net.lr.tinyhttp.Headers;
import net.lr.tinyhttp.HttpRequest;
import net.lr.tinyhttp.HttpResponse;
import net.lr.tinyhttp.MethodNotAllowedException;

public class SocketWorker implements Runnable {
    private Logger log = LoggerFactory.getLogger(SocketWorker.class);
    private Socket socket;
    private Map<String, Handler> handlers;
    private AtomicBoolean running;

    public SocketWorker(Socket socket, Map<String, Handler> handlers, AtomicBoolean running) {
        this.socket = socket;
        this.handlers = handlers;
        this.running = running;
    }

    @Override
    public void run() {
        try (
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream()) {
            boolean keepAlive = true;
            while (keepAlive && running.get()) {
                if (in.available() > 0) {
                    HttpRequest request = new HttpRequest(in);
                    HttpResponse response = new HttpResponse(request.getProtocolVersion(), out);
                    if (Headers.VALUE_CLOSE.equals(request.getHeader(Headers.CONNECTION))) {
                        keepAlive = false;
                    } else {
                        handle(request, response);
                        if (Headers.VALUE_CLOSE.equals(response.getHeader(Headers.CONNECTION))) {
                            keepAlive = false;
                        }
                        
                        out.flush();
                        //keepAlive = false;
                    }
                } else {
                    synchronized (in) {
                        in.wait(10);
                    }
                }
            }
            socket.shutdownOutput();
            drain(in);
            //System.out.println("closed connection");
            socket.close();
        } catch (Exception e) {
            if (running.get()) {
                //throw new RuntimeException(e);
                log.warn("Error handling request", e);
            }
        }
    }

    private void drain(InputStream in) {
        try {
            while (in.available() > 0) {
                in.read();
            }
        } catch (IOException e) {
            log.warn("Error draining input from socket" ,e);
        }
    }

    private void handle(HttpRequest request, HttpResponse response) throws IOException {
        try {
            for (Entry<String, Handler> entry : handlers.entrySet()) {
                if (request.getPath().startsWith(entry.getKey())) {
                    Handler handler = entry.getValue();
                    handler.process(entry.getKey(), request, response);
                    return;
                }
            }
            response.notFound();
        } catch (MethodNotAllowedException e) {
            response.methodNotAllowed();
        }
    }


}
