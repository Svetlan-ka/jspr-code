package ru.netology;

import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.net.URLEncodedUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;


public class Request {
    private final String method;
    private final String path;
    private List<NameValuePair> params = new ArrayList<>();
    private final BufferedReader body;


    public Request(String method, String path, List<NameValuePair> queryString, BufferedReader body) {
        this.method = method;
        this.path = path;
        this.params = queryString;
        this.body = body;
    }


    public static Request parse(BufferedReader in) throws IOException {

        final var requestLine = in.readLine();
        final var parts = requestLine.split(" ");

        if (parts.length != 3) {
            return null;
        }

        final var method = parts[0];
        final var delimeterPathQuery = parts[1].split(Pattern.quote("?"));
        final var path = delimeterPathQuery[0];
        final var queryString = URLEncodedUtils.parse(delimeterPathQuery[1], StandardCharsets.UTF_8);

        return new Request(method, path, queryString, in);
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
}
