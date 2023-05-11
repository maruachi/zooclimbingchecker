package org.example;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;

public class MessageReader implements Closeable {
    public static final byte[] EMPTY_BYTE = new byte[0];
    public static final String EMPTY_STRING = "";
    public static final int EMPTY = -1;
    public static final int MAX_SIZE = 8192;
    private final int bufferSize;
    private final int newLineStorageSize;

    private final BufferedInputStream bis;

    public MessageReader(int bufferSize, int newLineStorageSize, BufferedInputStream bis) {
        if (bufferSize <= 0 || newLineStorageSize <= 0) {
            throw new RuntimeException("내부 저장 공간 크기가 유효하지 않습니다. bufferSize = "
                    + bufferSize + " newLineStorageSize = " + newLineStorageSize);
        }

        if (bufferSize > MAX_SIZE || newLineStorageSize > MAX_SIZE) {
            throw new RuntimeException("내부 저장 공간의 최대 크기 " + MAX_SIZE + " 보다 큰 저장 공간을 사용할 수 없습니다. bufferSize = "
                    + bufferSize + " newLineStorageSize = " + newLineStorageSize);
        }

        if (bis == null) {
            throw new RuntimeException("스트림이 널 값을 참조합니다. (bis is null)");
        }

        this.bufferSize = bufferSize;
        this.newLineStorageSize = newLineStorageSize;
        this.bis = bis;
    }

    public static MessageReader createDefault(InputStream inputStream) {
        return new MessageReader(MAX_SIZE, MAX_SIZE, new BufferedInputStream(inputStream, MAX_SIZE));
    }

    public byte[] read() {
        //1. null처리나 터트리지 않고 서비스를 계속 유지 하려는 니즈
        //2. dto(buffer, len) / buffer with legnth len / int len = read(buffer)
        byte[] buffer = new byte[bufferSize];
        try {
            int len = bis.read(buffer);
            if (len == -1) {
                return EMPTY_BYTE;
            }

            if (len == bufferSize) {
                return buffer;
            }

            return Arrays.copyOfRange(buffer, 0, len);
        } catch (IOException e) {
            //데이터 없는 게 아닌데, 예외에 알맞게 처리한 것이 아니라 맥락에 맞지 않은 return이다.
            //스트림이 끊난 것이 아니라 예외처리이다. 사용자가 예외처리를 알도록 처리하자.
            //return EMPTY_BYTE;
            throw new RuntimeException(e);
        }
    }

    //생성자에서 받는 게 낫다.
    //파라미터로 받는 것은 10번은 utf-8, 8번은 ascii 이렇게 읽어줘! 이런 니즈를 처리하기 위한 형태이다.
    //이건 상당히 복잡한 처리라고 할 수 있다.
    //차라리 생성자로 받아서 필요한 처리를 인코딩을 고정해서 사용하는 것이 좋다.
    //public String readWith(Charset charset) {
    //    return new String(read(), charset);
    //}

    public String readLineWith(Charset charset) {
        byte[] newLineStorage = new byte[newLineStorageSize];
        try {
            bis.mark(1);
            int len = bis.read(newLineStorage);
            if (len == -1) {
                bis.reset();
                //hasMoreMessage() -> boolean함수랑 협동해서 처리하기
                //null하지 않고 empty_string으로 하는 이유는 null 처리시 인지적으로 이해하기 어려워진다.
                //그리고 계속 예외처리를 사용측에서 책임져야 한다. 즉 내부를 알고 써야 한다. 사용하기가 어렵다.
                return EMPTY_STRING;
            }
            
            int newLineIndex = findNewLineIndex(newLineStorage, len);
            if (newLineIndex == -1) {
                bis.reset();
                throw new RuntimeException("개행문자를 찾지 못했습니다.");
            }

            int newLineLength = newLineIndex;
            bis.reset();
            bis.skip(newLineIndex+1);

            return new String(newLineStorage, 0, newLineLength, charset);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean hasMoreMessage() {
        try {
            //available한다음에 buffer로 읽어서 처리하기
            if (bis.available() == 0) {
                return false;
            }
            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean doesNotMoreString() {
        return !hasMoreMessage();
    }

    private int findNewLineIndex(byte[] bytes, int len) {
        for (int i = 0; i < len; i++) {
            if (bytes[i] == '\n') {
                return i;
            }
        }
        return EMPTY;
    }

    @Override
    public void close() throws IOException {
        bis.close();
    }
}
