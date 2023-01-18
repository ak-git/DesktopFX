package com.ak.logging;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.NoSuchAlgorithmException;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogBuildersTest {
  static Stream<Path> logBuilders() {
    return Stream.of(LogBuilders.values())
        .map(binaryLogBuilder -> {
          try {
            return binaryLogBuilder.build(OutputBuildersTest.randomFileName()).getPath();
          }
          catch (IOException | NoSuchAlgorithmException e) {
            return null;
          }
        });
  }

  @ParameterizedTest
  @MethodSource("logBuilders")
  void testLogBuilders(Path path) throws IOException {
    assertNotNull(path);
    WritableByteChannel channel = Files.newByteChannel(path,
        StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
    channel.write(ByteBuffer.wrap(LogBuildersTest.class.getName().getBytes(Charset.defaultCharset())));
    channel.close();
    assertTrue(Files.deleteIfExists(path));
  }

  @ParameterizedTest
  @EnumSource(mode = EnumSource.Mode.EXCLUDE, value = LogBuilders.class, names = "CONVERTER_FILE")
  void testNotToClean(@Nonnull LogBuilders logBuilders) {
    Assertions.assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(logBuilders::clean);
  }

  @Test
  void testClean() throws IOException, NoSuchAlgorithmException {
    Path path = LogBuilders.CONVERTER_FILE.build(OutputBuildersTest.randomFileName()).getPath();
    WritableByteChannel channel = Files.newByteChannel(path,
        StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
    channel.write(ByteBuffer.wrap(LogBuildersTest.class.getName().getBytes(Charset.defaultCharset())));
    channel.close();
    LogBuilders.CONVERTER_FILE.clean();
    assertTrue(Files.notExists(path));
  }
}