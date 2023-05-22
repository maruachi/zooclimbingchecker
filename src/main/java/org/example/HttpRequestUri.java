package org.example;

import java.nio.file.Path;
import java.nio.file.Paths;

public class HttpRequestUri {
    private final String uri;

    public HttpRequestUri(String uri) {
        if (uri == null) {
            throw new RuntimeException("널 값을 참조하고 있습니다.");
        }

        if (!uri.startsWith("/")) {
            throw new RuntimeException("http 요청 uri 기본 형식과 일치하지 않습니다. uri = " + uri);
        }

        this.uri = uri;
    }

    public Path toPath() {
        return Paths.get(uri);
    }
}
