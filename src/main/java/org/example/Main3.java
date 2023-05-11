package org.example;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Main3 {

    public static final Charset US_ASCII = StandardCharsets.US_ASCII;

    public static void main(String[] args) {
        ServerSocket serverSocket = createServerSocket(7777);

        Socket clientSocket = listenClient(serverSocket);

        try (MessageReader messageReader = MessageReader.createDefault(clientSocket.getInputStream())) {
            if (messageReader.doesNotMoreString()) {
                responseFail();
                return;
            }

            String startLine = messageReader.readLineWith(US_ASCII);
            System.out.println(startLine);
            List<String> startLineElements = Arrays.stream(startLine.split(" "))
                    .filter(e -> !e.isEmpty())
                    .collect(Collectors.toUnmodifiableList());

            if (startLineElements.size() != 3) {
                responseFail();
                return;
            }

            //시스템 입력으로 바꾸기
            HttpMethod httpMethod = HttpMethod.of(startLineElements.get(0));
            String httpRequestTarget = startLineElements.get(1);
            String httpVersion = startLineElements.get(2);

            //System.out.println(httpMethod);
            //System.out.println(httpRequestTarget);
            //System.out.println(httpVersion);

            HashMap<String, List<String>> httpHeader = new HashMap<>();

            while (true) {
                String line = messageReader.readLineWith(US_ASCII);
                if ("".equals(line) || "\r".equals(line)) {
                    break;
                }

                int keyIndex = line.indexOf(':');
                if (keyIndex == -1) {
                    responseFail();
                    return;
                }
                String key = line.substring(0, keyIndex).trim();
                if (key.isBlank()) {
                    responseFail();
                    return;
                }
                String valueLine = line.substring(keyIndex + 1).trim();

                List<String> values = Arrays.stream(valueLine.split(","))
                        .filter(e -> !e.isBlank())
                        .map(String::trim)
                        .collect(Collectors.toUnmodifiableList());

                httpHeader.put(key, values);
            }

            if (httpMethod == HttpMethod.GET) {
                Path path = Paths.get(httpRequestTarget.substring(1));

                if (Files.notExists(path) || Files.isDirectory(path)) {
                    responseFail();
                    return;
                }

                OutputStream bos = new BufferedOutputStream(System.out, 8192);
                byte[] buffer = new byte[8192];
                try (InputStream bis = new BufferedInputStream(new FileInputStream(path.toFile()), 8192);) {
                    while (true) {
                        int len = bis.read(buffer);
                        if (len == -1) {
                            break;
                        }
                        bos.write(buffer, 0, len);
                    }
                    bos.flush();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                    responseFail();
                    return;
                }
            }


        } catch (IOException ioException) {
            throw new RuntimeException(ioException);
        }
    }

    private static void responseFail() {
        return;
    }

    private static Socket listenClient(ServerSocket serverSocket) {
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
