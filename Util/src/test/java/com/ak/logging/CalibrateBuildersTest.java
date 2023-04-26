package com.ak.logging;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.NoSuchAlgorithmException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CalibrateBuildersTest {
  static Stream<Path> builders() throws NoSuchAlgorithmException, IOException {
    return Stream.of(CalibrateBuilders.build(OutputBuildersTest.randomFileName()).getPath());
  }

  @ParameterizedTest
  @MethodSource("builders")
  void testBuild(Path path) throws IOException {
    assertNotNull(path);
    WritableByteChannel channel = Files.newByteChannel(path,
        StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
    channel.write(ByteBuffer.wrap(LogBuildersTest.class.getName().getBytes(Charset.defaultCharset())));
    channel.close();
    CalibrateBuilders.clean();
    assertTrue(Files.notExists(path));
  }
}