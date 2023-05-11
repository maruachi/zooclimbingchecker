package org.example;

import net.bytebuddy.implementation.bytecode.ShiftRight;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.assertj.core.api.AssertionsForClassTypes.in;

class MessageReader2Test {

    public static final Charset UTF_8 = StandardCharsets.UTF_8;

    //생성자
    @Test
    @ParameterizedTest
    @CsvSource({"1,0", "0,1", "0,0"})
    void 저장공간_크기가_0보다_같거나_작을_때_생성이_불가능한가(int bufferSize, int newLineStorageSize) {
        //given
        BufferedInputStream emptyBis = new BufferedInputStream(new ByteArrayInputStream(new byte[0]), 8192);

        //when
        Throwable actual = catchThrowable(() -> new MessageReader2(emptyBis, UTF_8, bufferSize, newLineStorageSize));

        //then
        Assertions.assertThat(actual).isInstanceOf(RuntimeException.class);
    }

    @Test
    @ParameterizedTest
    @CsvSource({"8193,1", "1,8193", "8193,8193"})
    void 저장공간_크가가_최대값보다_클_때_생성이_불가능한가(int bufferSize, int newLineStorageSize) {
        //given
        BufferedInputStream emptyBis = new BufferedInputStream(new ByteArrayInputStream(new byte[0]), 8192);

        //when
        Throwable actual = catchThrowable(() -> new MessageReader2(emptyBis, UTF_8, bufferSize, newLineStorageSize));

        //then
        Assertions.assertThat(actual).isInstanceOf(RuntimeException.class);
    }

    @Test
    void 스트림이_널_값을_참조할_때_생서이_불가능한가() {
        //given
        BufferedInputStream nullBis = null;

        //when
        Throwable actual = catchThrowable(() -> new MessageReader2(nullBis, UTF_8, 0,0));

        //then
        Assertions.assertThat(actual).isInstanceOf(RuntimeException.class);
    }

    @Test
    void 인코딩이_널_값을_참조할_때_생서이_불가능한가() {
        //given
        BufferedInputStream emptyBis = new BufferedInputStream(new ByteArrayInputStream(new byte[0]), 8192);
        Charset charset = null;

        //when
        Throwable actual = catchThrowable(() -> new MessageReader2(emptyBis, charset, 0,0));

        //then
        Assertions.assertThat(actual).isInstanceOf(RuntimeException.class);
    }

    //read()
    @Test
    void 바이트_입력_데이터가_저장공간을_초과했을_때_저장공간_크기만큼_읽는가() {
        //given
        byte[] bytes = new byte[]{
                1, 2, 3
        };
        MessageReader2 messageReader2 = MessageReader2.create(new ByteArrayInputStream(bytes), UTF_8, 2, 1);

        //when
        byte[] actual = messageReader2.read();

        //then
        Assertions.assertThat(actual).isEqualTo(new byte[]{1,2});
    }

    @Test
    void 바이트_입력_데이터를_연속해서_읽을_수_있는가() {
        //given
        byte[] bytes = new byte[]{
                1, 2, 3
        };
        MessageReader2 messageReader2 = MessageReader2.create(new ByteArrayInputStream(bytes), UTF_8, 2, 1);

        //when
        byte[] actual1 = messageReader2.read();
        byte[] actual2 = messageReader2.read();

        //then
        Assertions.assertThat(actual1).isEqualTo(new byte[]{1,2});
        Assertions.assertThat(actual2).isEqualTo(new byte[]{3});
    }

    @Test
    void 스트림_끝에_도달_했을_때_빈_바이트_배열로_처리되는가() {
        //given
        byte[] bytes = new byte[]{
                1, 2, 3
        };
        MessageReader2 messageReader2 = MessageReader2.create(new ByteArrayInputStream(bytes), UTF_8, 2, 1);
        messageReader2.read();
        messageReader2.read();

        //when
        byte[] actual = messageReader2.read();

        //then
        Assertions.assertThat(actual).isEqualTo(new byte[0]);
    }

    @Test
    void 읽기_에러_발생_시에_프로세서가_종료되는가() {
        //???
    }

    //readLine()
    @Test
    void 개행문자_단위로_끊어서_읽는가() {
        //given
        String input = "hello world\nmy name is dongyu\n";
        byte[] bytes = input.getBytes(UTF_8);
        MessageReader2 messageReader2 = MessageReader2.createDefault(new ByteArrayInputStream(bytes), UTF_8);

        //when
        String actual = messageReader2.readLine();

        //then
        Assertions.assertThat(actual).isEqualTo("hello world");
    }

    @Test
    void 연속적인_라인을_읽을_수_있는가() {
        //given
        String input = "hello world\nmy name is donggyu\n";
        byte[] bytes = input.getBytes(UTF_8);
        MessageReader2 messageReader2 = MessageReader2.createDefault(new ByteArrayInputStream(bytes), UTF_8);

        //when
        String actual1 = messageReader2.readLine();
        String actual2 = messageReader2.readLine();

        //then
        Assertions.assertThat(actual1).isEqualTo("hello world");
        Assertions.assertThat(actual2).isEqualTo("my name is donggyu");
    }

    @Test
    void 저장공간_내에_개항문자가_없을_때_예외처리가_되는가() {
        //given
        String input = "hello world\nmy name is donggyu\n";
        byte[] bytes = input.getBytes(UTF_8);
        MessageReader2 messageReader2 = MessageReader2.create(new ByteArrayInputStream(bytes), UTF_8, 1, 1);

        //when
        Throwable throwable = catchThrowable(() -> messageReader2.readLine());

        //then
        Assertions.assertThat(throwable).isInstanceOf(RuntimeException.class);
    }

    @Test
    void 스트림_끝에_도달했을_때_빈문자열로_처리되는가() {
        //given
        String input = "hello world\nmy name is donggyu\n";
        byte[] bytes = input.getBytes(UTF_8);
        MessageReader2 messageReader2 = MessageReader2.createDefault(new ByteArrayInputStream(bytes), UTF_8);
        messageReader2.readLine();
        messageReader2.readLine();

        //when
        String actual = messageReader2.readLine();

        //then
        Assertions.assertThat(actual).isEqualTo("");
    }

    @Test
    void 읽을_데이터의_존재_유무에_따라_참값이_올바르게_처리되는가() {
        //given
        String input = "hello word\n";
        byte[] bytes = input.getBytes(UTF_8);
        MessageReader2 messageReader2 = MessageReader2.createDefault(new ByteArrayInputStream(bytes), UTF_8);

        //when
        boolean actual1 = messageReader2.hasMoreMessage();
        messageReader2.readLine();
        boolean actual2 = messageReader2.hasMoreMessage();

        //then
        Assertions.assertThat(actual1).isTrue();
        Assertions.assertThat(actual2).isFalse();
    }

    @Test
    void 읽을_데이터의_존재_유무_판별_후에_읽을_데이터가_그대로_유지가되는가() {
        //given
        String input = "hello world\n";
        byte[] bytes = input.getBytes(UTF_8);
        MessageReader2 messageReader2 = MessageReader2.createDefault(new ByteArrayInputStream(bytes), UTF_8);

        //when
        messageReader2.hasMoreMessage();
        String actual = messageReader2.readLine();

        //then
        Assertions.assertThat(actual).isEqualTo("hello world");
    }

    @Test
    void 저장공간보다_큰_데이터를_반복하여_끝_판별하여_끝까지_읽을_수_있는가() {
        //given
        String input = "hello world\nmy name is donggyu\n";
        byte[] bytes = new byte[]{1,2,3,4,5,6,7,8,9};
        InputStream inputStream = new ByteArrayInputStream(bytes);
        MessageReader2 messageReader2 = MessageReader2.create(inputStream, UTF_8, 2, 1);

        //when
        byte[] actual = new byte[9];
        int cursor = 0;
        while (messageReader2.hasMoreMessage()) {
            byte[] readBytes = messageReader2.read();
            int readLength = readBytes.length;
            System.arraycopy(readBytes, 0, actual, cursor, readLength);
            cursor += readLength;
        }

        //then
        Assertions.assertThat(actual).isEqualTo(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9});
    }

    @Test
    void 문자열_데이터와_바이트_데이트를_번갈아_읽을_수_있는가() {
        //given
        String input = "hello world\nmy name is donggyu\n";
        byte[] bytes = input.getBytes(UTF_8);
        MessageReader2 messageReader2 = MessageReader2.createDefault(new ByteArrayInputStream(bytes), UTF_8);

        //when
        String actual1 = messageReader2.readLine();
        byte[] actual2 = messageReader2.read();

        //then
        Assertions.assertThat(actual1).isEqualTo("hello world");
        Assertions.assertThat(actual2).isEqualTo("my name is donggyu\n".getBytes(UTF_8));
    }
}
