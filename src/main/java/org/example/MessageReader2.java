package org.example;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;

public class MessageReader2 {
    public static final int MAX_SIZE = 8192;
    public static final int EOF = -1;
    public static final String EMPTY_STRING = "";
    public static final int NOT_FOUND_NEWLINE = -1;
    private final BufferedInputStream bis;
    private final Charset encoding;
    private final int bufferSize;
    private final int newLineStorageSize;
    private final byte[] buffer;
    private final byte[] newLineStorage;

    public MessageReader2(BufferedInputStream bis, Charset encoding, int bufferSize, int newLineStorageSize) {
        if (bufferSize <= 0 || newLineStorageSize <= 0) {
            throw new RuntimeException("내부 저장 공간 크기가 유효하지 않습니다. bufferSize = "
                    + bufferSize + " newLineStorageSize = " + newLineStorageSize);
        }

        if (bufferSize > MAX_SIZE || newLineStorageSize > MAX_SIZE) {
            throw new RuntimeException("내부 저장 공간의 최대 크기 " + MAX_SIZE + " 보다 큰 저장 공간을 사용할 수 없습니다. bufferSize = "
                    + bufferSize + " newLineStorageSize = " + newLineStorageSize);
        }

        if (bis == null) {
            throw new RuntimeException("스트림이 널 값을 참조합니다.");
        }

        if (encoding == null) {
            throw new RuntimeException("인코딩이 널 값을 참조합니다.");
        }

        this.bis = bis;
        this.encoding = encoding;
        this.bufferSize = bufferSize;
        this.newLineStorageSize = newLineStorageSize;

        this.buffer = new byte[this.bufferSize];
        this.newLineStorage = new byte[this.newLineStorageSize];
    }

    public static MessageReader2 create(InputStream inputStream, Charset encoding, int bufferSize, int newLineStorageSize) {
        return new MessageReader2(new BufferedInputStream(inputStream, bufferSize), encoding, bufferSize, newLineStorageSize);
    }
    public static MessageReader2 createDefault(InputStream inputStream, Charset encoding) {
        return new MessageReader2(new BufferedInputStream(inputStream, MAX_SIZE), encoding, MAX_SIZE, MAX_SIZE);
    }

    public byte[] read() {
        try {
            int len = bis.read(buffer);
            if (len == EOF) {
                return new byte[0];
            }
            return Arrays.copyOfRange(buffer, 0, len);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String readLine() {
        try {
            bis.mark(1);
            int len = bis.read(newLineStorage);
            if (len == EOF) {
                bis.reset();
                return EMPTY_STRING;
            }

            int newLineIndex = findNewLineIndex(len);
            if (newLineIndex == NOT_FOUND_NEWLINE) {
                bis.reset();
                throw new RuntimeException("개행문자를 찾을 수 없습니다.");
            }

            bis.reset();
            bis.skip(newLineIndex + 1);

            return new String(newLineStorage, 0, newLineIndex, encoding);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean hasMoreMessage() {
        try {
            bis.mark(1);
            int len = bis.read();
            if (len == -1) {
                bis.reset();
                return false;
            }

            bis.reset();
            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean doesNotHasMoreMessage() {
        return !hasMoreMessage();
    }

    private int findNewLineIndex(int len) {
        for (int i = 0; i < len; i++) {
            if (newLineStorage[i] == '\n') {
                return i;
            }
        }
        return NOT_FOUND_NEWLINE;
    }
}
