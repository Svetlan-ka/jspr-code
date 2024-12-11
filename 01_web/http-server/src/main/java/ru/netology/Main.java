package ru.netology;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;


public class Main {
    public static void main(String[] args) {
        final var server = new Server();

        // добавление хендлеров (обработчиков)
        server.addHandler("GET", "/index.html", (Main::defaultHandler));

        server.addHandler("POST", "/messages", (request, responseStream) ->
                System.out.println("Hendler с методом POST и путем /messages"));

        server.listen(8080);
    }

    public static void defaultHandler(Request request, BufferedOutputStream responseStream) throws IOException {
        final var path = request.getPath();
        final var filePath = Path.of(".", "public", path);
        final var mimeType = Files.probeContentType(filePath);

        final var length = Files.size(filePath);
        responseStream.write((
                "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + mimeType + "\r\n" +
                        "Content-Length: " + length + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        Files.copy(filePath, responseStream);
        responseStream.flush();
    }
}






