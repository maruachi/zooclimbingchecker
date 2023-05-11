package org.example;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;

class MessageReaderTest {
    @Test
    void 제한된_용량만큼_데이터를_읽는가() {
        //given
        byte[] bytes = new byte[]{
                1,2,3,4,5,6,7,8,9,10
        };

        InputStream inputStream = new ByteArrayInputStream(bytes);
        BufferedInputStream bis = new BufferedInputStream(inputStream, 8192);
        MessageReader messageReader = new MessageReader(5, 5, bis);

        //when
        byte[] actual = messageReader.read();

        //then
        byte[] expect = new byte[]{
                1,2,3,4,5
        };

        assertThat(actual).isEqualTo(expect);
    }

    @Test
    void 저장공간이_여유_있을_때_주어진_데이터의_크기만큼만_읽는가() {
        //given
        byte[] bytes = new byte[]{
                1,2,3,4,5
        };

        InputStream inputStream = new ByteArrayInputStream(bytes);
        BufferedInputStream bis = new BufferedInputStream(inputStream, 8192);
        MessageReader messageReader = new MessageReader(10, 5, bis);

        //when
        byte[] actual = messageReader.read();

        //then
        byte[] expect = new byte[]{
                1,2,3,4,5
        };

        assertThat(actual).isEqualTo(expect);
    }

    @Test
    void 개행문자_단위로_끊어_읽는가() {
        //given
        String string = "hello world\nMy name is donggyu\n";
        byte[] bytes = string.getBytes(StandardCharsets.US_ASCII);
        InputStream inputStream = new ByteArrayInputStream(bytes);
        BufferedInputStream bis = new BufferedInputStream(inputStream, 8192);

        MessageReader messageReader = new MessageReader(5, 13, bis);

        //when
        String actual = messageReader.readLineWith(StandardCharsets.US_ASCII);

        //then
        Assertions.assertThat(actual).isEqualTo("hello world\n");
    }

    @Test
    void 연속하여_개항문자_단위로_끊어_읽는가() {
        //given
        String string = "hello world\nMy name is donggyu\n";
        byte[] bytes = string.getBytes(StandardCharsets.US_ASCII);
        InputStream inputStream = new ByteArrayInputStream(bytes);
        BufferedInputStream bis = new BufferedInputStream(inputStream, 8192);

        MessageReader messageReader = new MessageReader(5, 20, bis);

        //when
        List<String> actual = new ArrayList<>();
        while (true) {
            String line = messageReader.readLineWith(StandardCharsets.US_ASCII);
            if ("".equals(line)) {
                break;
            }
            actual.add(line);
        }

        //then
        Assertions.assertThat(actual).isEqualTo(List.of("hello world\n", "My name is donggyu\n"));
    }

    @Test
    void 저장공간이_부족할_때_개행문자_단위로_끊어읽기에_실패하고_예외가_발생하는가() {
        //given
        String string = "hello world\nMy name is donggyu\n";
        byte[] bytes = string.getBytes(StandardCharsets.US_ASCII);
        InputStream inputStream = new ByteArrayInputStream(bytes);
        BufferedInputStream bis = new BufferedInputStream(inputStream, 8192);

        MessageReader messageReader = new MessageReader(5, 12, bis);

        //when
        List<String> actual = new ArrayList<>();
        String line = messageReader.readLineWith(StandardCharsets.US_ASCII);

        //then
        Assertions.assertThat(line).isEqualTo("hello world\n");
        Assertions.assertThatThrownBy(() -> messageReader.readLineWith(StandardCharsets.US_ASCII)).isInstanceOf(RuntimeException.class);
    }

    @Test
    void 한글이_존재할_때_개항문자_단위로_끊어_읽는가() {
        //given
        String string = "안녕 world\nMy name is donggyu\n";
        byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
        InputStream inputStream = new ByteArrayInputStream(bytes);
        BufferedInputStream bis = new BufferedInputStream(inputStream, 8192);

        MessageReader messageReader = new MessageReader(5, 100, bis);

        //when
        String actual = messageReader.readLineWith(StandardCharsets.UTF_8);

        //then
        Assertions.assertThat(actual).isEqualTo("안녕 world\n");
    }

    @Test
    void 바이트_배열과_개항문자_단위의_읽기처리를_번갈아_처리가능한가() {
        //given
        //문자열로 한번에 처리해도 됨.모든건 어차피 바이트 배열이니깐
        //중요한 건 데이터 단순 통신의 니즈가 있는가 아니면 데이터 분삭 및 판별의 느지가 있는가 그 차이라...
        String line = "hello world\n";
        byte[] bytes1 = line.getBytes(StandardCharsets.US_ASCII);
        byte[] bytes2 = new byte[]{
                1, 2, 3, 4, 5, 6, 7, 8, 9
        };

        InputStream inputStream1 = new ByteArrayInputStream(bytes1);
        InputStream inputStream2 = new ByteArrayInputStream(bytes2);
        InputStream inputStream = new SequenceInputStream(inputStream1, inputStream2);
        BufferedInputStream bis = new BufferedInputStream(inputStream, 8192);
        MessageReader messageReader = MessageReader.createDefault(bis);

        //when
        List<Object> actual = new ArrayList<>();
        actual.add(messageReader.readLineWith(StandardCharsets.US_ASCII));
        actual.add(messageReader.read());

        //then
        Assertions.assertThat(actual).isEqualTo(List.of(
                "hello world\n", new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9}
        ));
    }

    @Test
    void 유효성검사() {
        //given
        int bufferSize = 0;

        //when
        Throwable throwable = catchThrowable(() -> new MessageReader(bufferSize, 30, new BufferedInputStream(null, 8192)));

        //then
        Assertions.assertThat(throwable).doesNotThrowAnyException();
    }
}