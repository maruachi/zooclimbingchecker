package org.example;

import java.io.*;

public class BodySender {
    public static final int MAX_SIZE = 8192;
    private final BufferedOutputStream bos;
    private final int bufferSize;
    private final byte[] buffer;

    public BodySender(BufferedOutputStream bos, int bufferSize) {
        if (bos == null) {
            throw new RuntimeException("널 값을 참조하고 있습니다.");
        }

        if (bufferSize < 0 || bufferSize > MAX_SIZE) {
            throw new RuntimeException("유효한 값이 아닙니다. bufferSize = " + bufferSize);
        }
        this.bos = bos;
        this.bufferSize = bufferSize;

        this.buffer = new byte[bufferSize];
    }

    public static BodySender createDefault(OutputStream outputStream) {
        BufferedOutputStream bos = new BufferedOutputStream(outputStream, MAX_SIZE);
        return new BodySender(bos, MAX_SIZE);
    }

    public void sendNewLine() {
        try {
            bos.write('\n');
            bos.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void send(InputStream inputStream) {
        InputStream bis = toBuffered(inputStream);
        try {
            while (true) {
                int len = inputStream.read(buffer);
                if (len == -1) {
                    break;
                }
                bos.write(buffer, 0, len);
            }
            bos.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private InputStream toBuffered(InputStream inputStream) {
        return new BufferedInputStream(inputStream, bufferSize);
    }
}
