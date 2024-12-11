package ru.netology;

import java.io.BufferedReader;
import java.io.IOException;


public class Request {
    private final String method;
    private final String path;
    private final BufferedReader body;

    public Request(String method, String path, BufferedReader body) {
        this.method = method;
        this.path = path;
        this.body = body;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public BufferedReader getBody() {
        return body;
    }

    public static Request parse(BufferedReader in) throws IOException {

        final var requestLine = in.readLine();
        final var parts = requestLine.split(" ");

        if (parts.length != 3) {
            return null;
        }

        final var method = parts[0];
        final var path = parts[1];

        return new Request(method, path, in);
    }
}
