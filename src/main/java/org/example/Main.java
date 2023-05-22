package org.example;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class Main {

    public static final int NOT_FOUND = -1;
    public static final int BUFFER_SIZE = 8192;

    public static void main(String[] args) {
        ServerSocket serverSocket = createServerSocket(7777);

        Socket clientSocket = listen(serverSocket);

        InputStream clientInputStream = openInputStream(clientSocket);
        MessageReader messageReader = MessageReader.createDefault(clientInputStream, StandardCharsets.US_ASCII);

        String startLine = messageReader.readLine();
        HttpStartLine httpStartLine = HttpStartLine.parse(startLine);

        Map<String, String> httpHeader = new HashMap<>();
        while (true) {
            String headerLine = messageReader.readLine();
            if (headerLine.isEmpty()) {
                break;
            }

            int delimiterIndex = headerLine.indexOf(':');
            if (delimiterIndex == NOT_FOUND) {
                throw new RuntimeException("유효한 header 형식이 아닙니다.");
            }

            String key = headerLine.substring(0, delimiterIndex).trim();
            String value = headerLine.substring(delimiterIndex + 1).trim();

            httpHeader.put(key, value);
        }

        //처리 컨텍스트
        //target path 찾기
        HttpRequestUri httpRequestUri = httpStartLine.getHttpRequestUri();
        Path targetFilePath = httpRequestUri.toPath();

        VirtualPath virtualPath = VirtualPath.with(Environment.DEV);

        if (virtualPath.isNotWithVirtualPath(targetFilePath)) {
            //실패처리
            return;
        }

        if (virtualPath.isFile(targetFilePath)) {
            //실패처리
            return;
        }

        InputStream targetInputStream = virtualPath.openFileInputStream(targetFilePath);

        HttpResponseLine httpResponseLine = HttpResponseLine.createSuccess();
        HttpResponseHeader httpResponseHeader = new HttpResponseHeader();
        httpResponseHeader.setHeader("key", "value");

        //출력 컨텍스트
        OutputStream outputStream = openOutputStream(clientSocket);
        Writer writer = toWriter(outputStream, StandardCharsets.US_ASCII);

        httpResponseLine.send(writer);
        httpResponseHeader.send(writer);

        BodySender bodySender = BodySender.createDefault(outputStream);

        bodySender.sendNewLine();
        bodySender.send(targetInputStream);
    }

    private static Writer toWriter(OutputStream outputStream, Charset encoding) {
        BufferedOutputStream bos = new BufferedOutputStream(outputStream, BUFFER_SIZE);
        return new OutputStreamWriter(bos, encoding);
    }

    private static OutputStream openOutputStream(Socket clientSocket) {
        try {
            return clientSocket.getOutputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static InputStream openInputStream(Socket clientSocket) {
        try {
            return clientSocket.getInputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Socket listen(ServerSocket serverSocket) {
        try {
            return serverSocket.accept();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static ServerSocket createServerSocket(int port) {
        try {
            return new ServerSocket(port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
