package org.example;

import java.util.Arrays;

public enum HttpReasonPhrase {
    OK("OK"), FAIL("FAIL");

    private final String value;

    HttpReasonPhrase(String value) {
        if (value == null) {
            throw new RuntimeException("널 값을 참조하고 있습니다.");
        }
        this.value = value;
    }

    public static HttpReasonPhrase of(String value) {
        return Arrays.stream(HttpReasonPhrase.values())
                .filter(reasonPhrase -> reasonPhrase.value.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("유효한 http reason phrase 값이 아닙니다. value = " + value));
    }
}