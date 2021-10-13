package com.ak.logging;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.NoSuchAlgorithmException;
import java.util.EnumSet;
import java.util.stream.Stream;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class LogBuildersTest {
  @DataProvider(name = "logBuilders")
  public static Object[][] logBuilders() {
    return Stream.of(LogBuilders.values())
        .map(binaryLogBuilder -> {
          try {
            return new Object[] {binaryLogBuilder.build(OutputBuildersTest.randomFileName()).getPath()};
          }
          catch (IOException | NoSuchAlgorithmException e) {
            return new Object[] {null};
          }
        })
        .toArray(Object[][]::new);
  }

  @Test(dataProvider = "logBuilders")
  public void testLogBuilders(Path path) throws IOException {
    Assert.assertNotNull(path);
    WritableByteChannel channel = Files.newByteChannel(path,
        StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
    channel.write(ByteBuffer.wrap(LogBuildersTest.class.getName().getBytes(Charset.defaultCharset())));
    channel.close();
    Assert.assertTrue(Files.deleteIfExists(path));
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testNotToClean() {
    EnumSet.complementOf(EnumSet.of(LogBuilders.CONVERTER_FILE)).forEach(LogBuilders::clean);
  }

  @Test
  public void testClean() throws IOException, NoSuchAlgorithmException {
    Path path = LogBuilders.CONVERTER_FILE.build(OutputBuildersTest.randomFileName()).getPath();
    WritableByteChannel channel = Files.newByteChannel(path,
        StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
    channel.write(ByteBuffer.wrap(LogBuildersTest.class.getName().getBytes(Charset.defaultCharset())));
    channel.close();
    LogBuilders.CONVERTER_FILE.clean();
    Assert.assertTrue(Files.notExists(path));
  }
}