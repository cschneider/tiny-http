package net.lr.tinyhttp.impl;

/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.lr.tinyhttp.Handler;
import net.lr.tinyhttp.HttpRequest;
import net.lr.tinyhttp.HttpResponse;
import net.lr.tinyhttp.MethodNotAllowedException;

public class Server implements Closeable, Runnable {
    private Logger log = LoggerFactory.getLogger(Server.class);
    private ServerSocket serverSocket;
    private boolean running;
    private ExecutorService executor;
    private Map<String, Handler> handlers;
    
    public Server() {
        this.handlers = new HashMap<>();
    }

    public void start(String localip, Integer port, int numThreads) {
        if (numThreads<1) {
            throw new IllegalArgumentException("Number of threads must be > 1 but was " + numThreads);
        }
        try {
            this.serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.running = true;
        this.executor = Executors.newCachedThreadPool();
        for (int c = 0; c < numThreads; c++) {
            this.executor.execute(this);
        }
    }

    public void run() {
        while (running) {
            try (Socket socket = this.serverSocket.accept();
                InputStream in = socket.getInputStream();
                OutputStream out = socket.getOutputStream()) {
                handle(in, out);
            } catch (Exception e) {
                if (running) {
                    log.warn("Error handling request", e);
                }
            }
        }
    }
    
    public void addHandler(String path, Handler handler) {
        this.handlers.put(path, handler);
    }
    
    public void removeHandler(String path) {
        this.handlers.remove(path);
    }

    private void handle(InputStream in, OutputStream out) throws IOException {
        HttpRequest request = new HttpRequest(in);
        HttpResponse response = new HttpResponse(request.getProtocolVersion(), out);
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

    @Override
    public void close() throws IOException {
        this.running = false;
        this.serverSocket.close();
        this.executor.shutdown();
        try {
            this.executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
        }
        this.executor.shutdownNow();
    }

}
