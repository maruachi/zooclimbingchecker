package org.example;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class VirtualPath {
    private final Path prefixPath;

    public VirtualPath(Path prefixPath) {
        if (prefixPath == null) {
            throw new RuntimeException("널 값을 참조하고 있습니다.");
        }
        this.prefixPath = prefixPath;
    }

    public static VirtualPath with(Environment environment) {
        return new VirtualPath(Paths.get(environment.getPrefixPath()));
    }

    public InputStream openFileInputStream(Path path) {
        if (isNotWithVirtualPath(path)) {
            throw new RuntimeException("가상 경로에 해당하지 않습니다. path = " + path);
        }

        if (!isFile(path)) {
            throw new RuntimeException("스트림을 열 수 없습니다. 파일이 아닙니다. path = " + path);
        }

        try {
            return Files.newInputStream(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isFile(Path path) {
        if (isNotWithVirtualPath(path)) {
            throw new RuntimeException("가상 경로에 해당하지 않습니다. path = " + path);
        }

        return Files.exists(path);
    }

    public boolean isWithinVirtualPath(Path path) {
        if (path == null) {
            throw new RuntimeException("널 값을 참조하고 있습니다.");
        }

        Path resolvedPath = prefixPath.resolve(path).normalize();

        return resolvedPath.startsWith(prefixPath);
    }

    public boolean isNotWithVirtualPath(Path path) {
        return !isWithinVirtualPath(path);
    }
}
