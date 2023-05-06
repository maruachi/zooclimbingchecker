package org.example;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class MessageReader implements Closeable {
    public static final Charset UTF_8 = StandardCharsets.UTF_8;
    public static final int MAX_SIZE = 8192;
    private final BufferedInputStream bufferedInputStream;

    private final int bufferSize;
    private final int newLineStorageSize;

    public MessageReader(BufferedInputStream bufferedInputStream, int bufferSize, int newLineStorageSize) {
        if (bufferedInputStream == null) {
            throw new RuntimeException("널 포인터를 참조합니다.");
        }

        if (bufferSize <= 0 || newLineStorageSize <= 0) {
            throw new RuntimeException("유효한 사이즈가 아닙니다. bufferSize = " + bufferSize + ", newLineStorageSize = " + newLineStorageSize);
        }

        if (bufferSize > MAX_SIZE || newLineStorageSize > MAX_SIZE) {
            throw new RuntimeException("최대값을 넘었습니다. bufferSize = " + bufferSize + ", newLineStorageSize = " + newLineStorageSize);
        }

        this.bufferedInputStream = bufferedInputStream;
        this.bufferSize = bufferSize;
        this.newLineStorageSize = newLineStorageSize;
    }

    public static MessageReader createDefault(InputStream inputStream) {
        return new MessageReader(new BufferedInputStream(inputStream, MAX_SIZE), MAX_SIZE, MAX_SIZE);
    }

    public String readLine() {
        char[] cbuffer = new char[newLineStorageSize];
        Reader reader = new InputStreamReader(bufferedInputStream, UTF_8);
        try {
            int len = reader.read(cbuffer);
            if (len == -1) {
                return null;
            }

            int newLineIndex = findNewLineIndex(cbuffer, len);
            if (newLineIndex == -1) {
                return null;
            }

            return new String(cbuffer, 0, newLineIndex);
        } catch (IOException ioException) {
            ioException.printStackTrace();
            throw new RuntimeException(ioException);
        }
    }

    public String readLine2() {
        StringBuilder stringBuilder = new StringBuilder(newLineStorageSize);
        
        while (true) {
            String string = readString();
            if (string == null) {
                break;
            }

            int newLineIndex = string.indexOf(' ');
            int newLineSubStringLength = newLineIndex;
            if (newLineSubStringLength + stringBuilder.length() > newLineSubStringLength) {
                return null;
            }

            if (newLineIndex >= 0) {
                stringBuilder.append(string.substring(0, newLineIndex));
                break;
            }

            stringBuilder.append(string);
        }

        return stringBuilder.toString();
    }

    private int findNewLineIndex(char[] cbuffer, int len) {
        for (int i = 0; i < len; i++) {
            if (cbuffer[i] == '\n') {
                return i;
            }
        }
        return -1;
    }

    public byte[] read() {
        byte[] bytes = new byte[bufferSize];
        try {
            int len = bufferedInputStream.read(bytes);
            if (len == -1) {
                return null;
            }
            return bytes;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String readString() {
        byte[] bytes = read();
        if (bytes == null) {
            return null;
        }
        return new String(bytes, UTF_8);
    }

    @Override
    public void close() throws IOException {
        bufferedInputStream.close();
    }
}
