package ru.netology;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Server {
    ExecutorService executorService;
    final ConcurrentHashMap<String, Handler> handlersGet = new ConcurrentHashMap<>();
    final ConcurrentHashMap<String, Handler> handlersPost = new ConcurrentHashMap<>();

    final static String GET = "GET";
    final static String POST = "POST";
    final int NUMBER_THREAD_POOL = 64;


    public Server() {
        executorService = Executors.newFixedThreadPool(NUMBER_THREAD_POOL);
    }


    public void addHandler(String method, String path, Handler handler) {

        if (method.equals(GET)) {
            if (!handlersGet.containsKey(path))
                handlersGet.put(path, handler);
        }

        if (method.equals(POST)) {
            if (!handlersPost.containsKey(path))
                handlersPost.put(path, handler);
        }
    }


    public void listen(int port) {
        try (final var serverSocket = new ServerSocket(port)) {
            while (true) {
                final var socket = serverSocket.accept();
                executorService.execute(() -> {
                    try {
                        processRequest(socket);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void processRequest(Socket socket) throws IOException {
        try (final BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             final BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream())) {

            Request request = Request.parse(in);

            if (request == null) {
                badRequest(out);
                return;
            }

            if (!request.getMethod().equals(GET) && !request.getMethod().equals(POST))
                badRequest(out);

            if (request.getMethod().equals(GET)) {
                if (handlersGet.containsKey(request.getPath())) {
                    handlersGet.get(request.getPath()).handle(request, out);
                } else {
                    notFound(out);
                }
            }

            if (request.getMethod().contains(POST)) {
                if (handlersPost.containsKey(request.getPath())) {
                    handlersPost.get(request.getPath()).handle(request, out);
                } else {
                    notFound(out);
                }
            }
        }
    }


    private void badRequest(BufferedOutputStream out) throws IOException {
        out.write((
                "HTTP/1.1 400 Bad Request\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
    }


    private void notFound(BufferedOutputStream out) throws IOException {
        out.write((
                "HTTP/1.1 404 Not Found\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
    }
}