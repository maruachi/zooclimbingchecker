package org.example;

import java.io.Writer;

public class HttpResponseLine {
    private final HttpVersion httpVersion;
    private final HttpStatusCode httpStatusCode;
    private final HttpReasonPhrase httpReasonPhrase;

    public HttpResponseLine(HttpVersion httpVersion, HttpStatusCode httpStatusCode, HttpReasonPhrase httpReasonPhrase) {
        if (httpVersion == null || httpStatusCode == null || httpReasonPhrase == null) {
            throw new RuntimeException("널 값을 참조하고 있습니다.");
        }

        this.httpVersion = httpVersion;
        this.httpStatusCode = httpStatusCode;
        this.httpReasonPhrase = httpReasonPhrase;
    }

    public static HttpResponseLine createSuccess() {
        HttpVersion httpVersion = new HttpVersion(1, 1);
        HttpStatusCode httpStatusCode = new HttpStatusCode(200);
        HttpReasonPhrase httpReasonPhrase = HttpReasonPhrase.OK;
        return new HttpResponseLine(httpVersion, httpStatusCode, httpReasonPhrase);
    }

    public static HttpResponseLine createFail() {
        HttpVersion httpVersion = new HttpVersion(1, 1);
        HttpStatusCode httpStatusCode = new HttpStatusCode(404);
        HttpReasonPhrase httpReasonPhrase = HttpReasonPhrase.FAIL;
        return new HttpResponseLine(httpVersion, httpStatusCode, httpReasonPhrase);
    }

    public void send(Writer writer) {
        return;
    }
}
