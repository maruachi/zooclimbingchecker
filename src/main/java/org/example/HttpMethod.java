package org.example;

import java.util.Arrays;

public enum HttpMethod {
    GET("GET");

    private final String value;

    HttpMethod(String value) {
        if (value == null) {
            throw new RuntimeException("널 값을 참조하였습니다.");
        }
        this.value = value;
    }

    public static HttpMethod of(String value) {
        return Arrays.stream(HttpMethod.values())
                .filter(method -> method.value.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("일치하는 http method가 없습니다. value = " + value));
    }
}
