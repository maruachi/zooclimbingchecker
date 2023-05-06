package org.example;

import javax.swing.*;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class MessageReader2 implements Closeable {

    public static final Charset UTF_8 = StandardCharsets.UTF_8;
    private static final int MAX_SIZE = 8192;
    private final int bufferSize;
    private final int newLineStorageSize;
    private final InputStream bufferedInputStream;
    private final byte[] storage;
    private int storageLength = 0;

    public MessageReader2(int bufferSize, int newLineStorageSize, InputStream bufferedInputStream, byte[] storage) {
        if (storage == null) {
            throw new RuntimeException("널 값을 참조합니다.");
        }
        if (bufferSize <= 0 || newLineStorageSize <= 0) {
            throw new RuntimeException("유효하지 않은 저장 공간 크기입니다. bufferSize = " + bufferSize + " newLineStorageSize = " + newLineStorageSize);
        }
        if (bufferSize > MAX_SIZE || newLineStorageSize > MAX_SIZE) {
            throw new RuntimeException("최대 크기를 초과합니다. bufferSize = " + bufferSize + " newLineStorageSize = " + newLineStorageSize);
        }

        this.bufferSize = bufferSize;
        this.newLineStorageSize = newLineStorageSize;
        this.bufferedInputStream = bufferedInputStream;
        this.storage = storage;
    }

    public static MessageReader2 createDefault(InputStream inputStream) {
        return new MessageReader2(MAX_SIZE, MAX_SIZE, new BufferedInputStream(inputStream, MAX_SIZE), new byte[MAX_SIZE]);
    }

    public byte[] read() {
        try {
            int len = bufferedInputStream.read(storage, storageLength, newLineStorageSize - storageLength);
            if (len == -1) {
                return null;
            }
            int totalLength = storageLength + len;
            storageLength = 0;
            return Arrays.copyOf(storage, totalLength);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String readByString() {
        byte[] bytes = read();
        if (bytes == null) {
            return null;
        }
        return new String(bytes, UTF_8);
    }

    public String readLine() {
        StringBuilder builder = new StringBuilder(newLineStorageSize);
        while (true) {
            String string = readByString();
            if (string == null) {
                break;
            }

            int newLineIndex = string.indexOf('\n');
            if (newLineIndex >= 0) {
                String subString = string.substring(0, newLineIndex - 1);
                builder.append(subString);

                byte[] afterNewLineBytes = string.substring(newLineIndex + 1).getBytes(UTF_8);

                System.arraycopy(afterNewLineBytes, 0, storage, 0, afterNewLineBytes.length);
                break;
            }

            builder.append(string);
        }
        return builder.toString();
    }

    @Override
    public void close() throws IOException {
        bufferedInputStream.close();
    }
}
