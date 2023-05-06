package org.example;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Main {

    public static final int PORT = 7777;
    public static final int BUFFER_SIZE = 8192;

    public static void main(String[] args) {
        ServerSocket serverSocket = createServerSocket();

        System.out.println("응답 대기 중");
        try (Socket clientSocket = serverSocket.accept()) {
            try (BufferedReader br = toReader(clientSocket.getInputStream());
                 Writer writer = toWriter(clientSocket.getOutputStream())) {
                while (true) {
                    String line = br.readLine();
                    if (line.isEmpty()) {
                        break;
                    }
                    System.out.println(line);
                }

                writer.write("HTTP/1.1 200 OK\n");
                writer.write("Content-type: text/plain\n");
                writer.write('\n');
                writer.write("hello world\n");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
            return;
        }
    }

    private static Writer toWriter(OutputStream outputStream) {
        OutputStream bos = new BufferedOutputStream(outputStream, BUFFER_SIZE);
        return new OutputStreamWriter(bos, StandardCharsets.UTF_8);
    }

    private static BufferedReader toReader(InputStream inputStream) {
        InputStream bis = new BufferedInputStream(inputStream, BUFFER_SIZE);
        InputStreamReader reader = new InputStreamReader(bis, StandardCharsets.UTF_8);
        return new BufferedReader(reader, BUFFER_SIZE);
    }

    private static ServerSocket createServerSocket() {
        try {
            return new ServerSocket(PORT);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}