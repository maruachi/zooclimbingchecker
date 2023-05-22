package org.example;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;

public class MessageReader {
    public static final int MAX_SIZE = 8192;
    public static final byte[] EMPTY_BYTE = new byte[0];
    public static final String EMPTY_STRING = "";
    public static final int NOT_FOUND = -1;
    private final BufferedInputStream bis;
    private final int bufferSize;
    private final int newLineStorageSize;
    private final Charset encoding;
    private final byte[] buffer;
    private final byte[] newLineStorage;

    public MessageReader(BufferedInputStream bis, int bufferSize, int newLineStorageSize, Charset encoding) {
        if (bis == null || encoding == null) {
            throw new RuntimeException("널  값을 참조하고 있습니다.");
        }

        if (bufferSize < 0 || newLineStorageSize < 0 || bufferSize > MAX_SIZE || newLineStorageSize > MAX_SIZE) {
            throw new RuntimeException("유효하지 않은 저장공간 크기입니다. bufferSize = " + bufferSize + " newLineStorageSize = " + newLineStorageSize);
        }
        this.bis = bis;
        this.bufferSize = bufferSize;
        this.newLineStorageSize = newLineStorageSize;
        this.encoding = encoding;

        this.buffer = new byte[bufferSize];
        this.newLineStorage = new byte[newLineStorageSize];
    }

    public static MessageReader create(InputStream inputStream, int bufferSize, int newLineStorageSize, Charset encoding) {
        BufferedInputStream bis = new BufferedInputStream(inputStream, bufferSize);

        return new MessageReader(bis, bufferSize, newLineStorageSize, encoding);
    }

    public static MessageReader createDefault(InputStream inputStream, Charset encoding) {
        return MessageReader.create(inputStream, MAX_SIZE, MAX_SIZE, encoding);
    }

    public byte[] read() {
        try {
            int len = bis.read(buffer);
            if (len == -1) {
                return EMPTY_BYTE;
            }

            return Arrays.copyOfRange(buffer, 0, len);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String readLine() {
        bis.mark(1);
        try {
            int len = bis.read(buffer);
            if (len == -1) {
                bis.reset();
                return EMPTY_STRING;
            }

            int newLineIndex = findNewLineIndex(buffer, len);
            if (newLineIndex == NOT_FOUND) {
                throw new RuntimeException("개행문자를 찾을 수 없습니다.");
            }
            bis.reset();
            bis.skip(newLineIndex+1);

            return new String(buffer, 0, newLineIndex, encoding);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private int findNewLineIndex(byte[] buffer, int len) {
        for (int i = 0; i < len; i++) {
            if (buffer[i] == '\n') {
                return i;
            }
        }
        return NOT_FOUND;
    }
}
