package ru.netology;

import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.net.URLEncodedUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import static ru.netology.Server.POST;


public class Request {
    private final String method;
    private final String path;
    private List<String> headers;
    private List<NameValuePair> params;
    private List<NameValuePair> body;

    public Request(String method, String path) {
        this.method = method;
        this.path = path;
    }

    public Request(String method, String path, List<String> headers) {
        this.method = method;
        this.path = path;
        this.headers = headers;
    }

    public Request(String method, String path, List<String> headers, List<NameValuePair> body) {
        this.method = method;
        this.path = path;
        this.headers = headers;
        this.body = body;
    }


    public static Request parse(BufferedInputStream in) throws IOException {

        // лимит на request line + заголовки
        final var limit = 4096;

        in.mark(limit);
        final var buffer = new byte[limit];
        final var read = in.read(buffer);

        // ищем request line
        final var requestLineDelimiter = new byte[]{'\r', '\n'};
        final var requestLineEnd = indexOf(buffer, requestLineDelimiter, 0, read);
        if (requestLineEnd == -1) return null;

        // читаем request line
        final var requestLine = new String(Arrays.copyOf(buffer, requestLineEnd)).split(" ");
        if (requestLine.length != 3) return null;

        final var method = requestLine[0];

        final var delimeterPathQuery = requestLine[1].split(Pattern.quote("?"));
        final var path = delimeterPathQuery[0];
        if (!path.startsWith("/")) return null;

        List<NameValuePair> queryString = new ArrayList<>();
        if (delimeterPathQuery.length == 2) {
            queryString = URLEncodedUtils.parse(delimeterPathQuery[1], StandardCharsets.UTF_8);
        }

        final var headersDelimiter = new byte[]{'\r', '\n', '\r', '\n'};
        final var headersStart = requestLineEnd + requestLineDelimiter.length;
        final var headersEnd = indexOf(buffer, headersDelimiter, headersStart, read);
        if (headersEnd == -1) return null;

        // отматываем на начало буфера
        in.reset();


        final var headersBytes = in.readNBytes(headersEnd - headersStart);
        final var headers = Arrays.asList(new String(headersBytes).split("\r\n"));

        //парсим тело запроса
        List<NameValuePair> bodyParse = new ArrayList<>();
        final var contentType = extractHeader(headers, "Content-Type");
        if (contentType.isPresent()) {
            final var type = contentType.get();

            if (method.equals(POST) & !type.isEmpty()) {
                if (type.equals("application/x-www-form-urlencoded")) {
                    // вычитываем Content-Length, чтобы прочитать body
                    final var contentLength = extractHeader(headers, "Content-Length");
                    if (contentLength.isPresent()) {
                        final var length = Integer.parseInt(contentLength.get());
                        final var bodyBytes = in.readNBytes(length);

                        final var body = new String(bodyBytes);
                        bodyParse = URLEncodedUtils.parse(body, StandardCharsets.UTF_8);
                    }
                }
            }
        }

        if (!headers.isEmpty() & !queryString.isEmpty()) return new Request(method, path, headers, queryString);
        if (!headers.isEmpty() & !bodyParse.isEmpty()) return new Request(method, path, headers, bodyParse);
        if (queryString.isEmpty() & bodyParse.isEmpty()) return new Request(method, path, headers);
        return new Request(method, path);
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public BufferedInputStream getBody() {
        return (BufferedInputStream) body;
    }

    public List<NameValuePair> getQueryParam(String name) {
        List<NameValuePair> searchByName = new ArrayList<>();
        for (NameValuePair param : params) {
            if (param.getName().equals(name))
                searchByName.add(param);
        }
        return searchByName;
    }

    public List<NameValuePair> getQueryParams() {
        return params;
    }

    public List<NameValuePair> getPostParam(String name) {
        List<NameValuePair> searchByBody = new ArrayList<>();
        for (NameValuePair param : body) {
            if (param.getName().equals(name))
                searchByBody.add(param);
        }
        return searchByBody;
    }

    public List<NameValuePair> getPostParams() {
        return body;
    }

    private static int indexOf(byte[] array, byte[] target, int start, int max) {
        outer:
        for (int i = start; i < max - target.length + 1; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }

    private static Optional<String> extractHeader(List<String> headers, String header) {
        return headers.stream()
                .filter(o -> o.startsWith(header))
                .map(o -> o.substring(o.indexOf(" ")))
                .map(String::trim)
                .findFirst();
    }

    @Override
    public String toString() {
        return "Request{" +
                "method='" + method + '\'' +
                ", path='" + path + '\'' +
                ", headers='" + headers + '\'' +
                ", params=" + params +
                ", body=" + body +
                '}';
    }
}
