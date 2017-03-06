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
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.lr.tinyhttp.Handler;

public class Server implements Closeable, Runnable {
    private Logger log = LoggerFactory.getLogger(Server.class);
    private int keepAliveTimeOut;
    private ServerSocket serverSocket;
    private AtomicBoolean running;
    private ExecutorService executor;
    private Map<String, Handler> handlers;

    /**
     * @param keepAliveTimeOut http connection will be kept open for keepAliveTimeOut seconds
     */
    public Server() {
        this.handlers = new ConcurrentHashMap<>();
    }

    public void start(String localip, Integer port, int numThreads, int keepAliveTimeOut) {
        if (numThreads < 1) {
            throw new IllegalArgumentException("Number of threads must be > 1 but was " + numThreads);
        }
        this.keepAliveTimeOut = keepAliveTimeOut;
        try {
            this.serverSocket = new ServerSocket(port);
            this.serverSocket.setReuseAddress(true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.running = new AtomicBoolean(true);
        this.executor = Executors.newFixedThreadPool(numThreads);
        new Thread(this).start();
    }

    public void run() {
        while (running.get()) {
            try {
                Socket socket = this.serverSocket.accept();
                socket.setSoTimeout(keepAliveTimeOut * 1000);
                executor.execute(new SocketWorker(socket, handlers, running));
            } catch (Exception e) {
                if (running.get()) {
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


    @Override
    public void close() throws IOException {
        this.running.set(false);
        this.serverSocket.close();
        this.executor.shutdown();
        try {
            this.executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
        }
        this.executor.shutdownNow();
    }

}
