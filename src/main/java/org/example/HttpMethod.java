package org.example;

import java.util.Arrays;

public enum HttpMethod {
    GET("GET");

    private final String value;

    HttpMethod(String value) {
        this.value = value;
    }

    public static HttpMethod of(String value) {
        return Arrays.stream(HttpMethod.values())
                .filter(httpMethod -> httpMethod.value.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("유효한 httpMethod 값이 아닙니다. value = " + value));
    }
}
