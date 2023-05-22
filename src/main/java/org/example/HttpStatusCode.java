package org.example;

public class HttpStatusCode {
    private final int value;

    public HttpStatusCode(int value) {
        if (value < 200 || value > 600) {
            throw new RuntimeException("유효한 status code가 아닙니다. value = " + value);
        }
        this.value = value;
    }
}
