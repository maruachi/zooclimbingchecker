package org.example;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class HttpStartLine {
    private final HttpMethod httpMethod;
    private final HttpRequestUri httpRequestUri;
    private final HttpVersion httpVersion;

    public HttpStartLine(HttpMethod httpMethod, HttpRequestUri httpRequestUri, HttpVersion httpVersion) {
        if (httpMethod == null || httpRequestUri == null || httpVersion == null) {
            throw new RuntimeException("널 값을 참조하고 있습니다.");
        }

        this.httpMethod = httpMethod;
        this.httpRequestUri = httpRequestUri;
        this.httpVersion = httpVersion;
    }

    public static HttpStartLine parse(String line) {
        if (line == null) {
            throw new RuntimeException("널 값을 참조하고 있습니다.");
        }

        List<String> lineElements = Arrays.stream(line.split(" "))
                .filter(e -> e.isBlank())
                .collect(Collectors.toUnmodifiableList());

        if (lineElements.size() != 3) {
            throw new RuntimeException("startLine의 요소 수가 맞지 않습니다. lineElement.size() = " + lineElements.size());
        }

        HttpMethod httpMethod = HttpMethod.of(lineElements.get(0));
        HttpRequestUri httpRequestUri = new HttpRequestUri(lineElements.get(1));
        HttpVersion httpVersion = HttpVersion.parse(lineElements.get(2));

        return new HttpStartLine(httpMethod, httpRequestUri, httpVersion);
    }

    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public HttpRequestUri getHttpRequestUri() {
        return httpRequestUri;
    }

    public HttpVersion getHttpVersion() {
        return httpVersion;
    }
}
