package org.example;

public enum Environment {
    DEV("DEV", "/Users/dgyim/IdeaProjects/zooclimbingchecker/src/main/resources"),
    PRODUCT("PRODUCT", "/");

    private final String value;
    private final String prefixPath;

    Environment(String value, String prefixPath) {
        if (value == null || prefixPath == null) {
            throw new RuntimeException("널 값을 참조하고 있습니다.");
        }
        this.value = value;
        this.prefixPath = prefixPath;
    }

    public String getPrefixPath() {
        return prefixPath;
    }
}
