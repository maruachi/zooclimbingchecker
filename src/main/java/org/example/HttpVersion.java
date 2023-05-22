package org.example;

public class HttpVersion {
    public static final int NOT_FOUND = -1;
    private final int major;
    private final int minor;

    public HttpVersion(int major, int minor) {
        if (major < 0 || minor < 0) {
            throw new RuntimeException("버전 값이 올바르지 않습니다. major = " + major + " minor = " + minor);
        }
        this.major = major;
        this.minor = minor;
    }

    public static HttpVersion parse(String value) {
        if (!value.toUpperCase().startsWith("HTTP/")) {
            throw new RuntimeException("http 버전이 아닙니다. value = " + value);
        }

        String version = value.substring("HTTP/".length());
        int delimiterIndex = version.indexOf('.');
        if (delimiterIndex == NOT_FOUND) {
            throw new RuntimeException("올바른 버전 포맷이 아닙니다. value = " + value);
        }

        int major = Integer.parseInt(version.substring(0, delimiterIndex));
        int minor = Integer.parseInt(version.substring(delimiterIndex+1));

        return new HttpVersion(major, minor);
    }
}
