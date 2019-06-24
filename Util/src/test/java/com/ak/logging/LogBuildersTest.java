package com.ak.logging;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class LogBuildersTest {
  private static final Object[][] EMPTY_OBJECTS = {};

  private LogBuildersTest() {
  }

  @DataProvider(name = "logBuilders")
  public static Object[][] logBuilders() {
    return Stream.of(LogBuilders.values()).
        map(binaryLogBuilder -> {
          try {
            return new Object[] {binaryLogBuilder.build("02f29f660fa69e6c404c03de0f1e15f9").getPath()};
          }
          catch (IOException e) {
            return new Object[] {null};
          }
        }).collect(Collectors.toList()).toArray(EMPTY_OBJECTS);
  }


  @Test(dataProvider = "logBuilders")
  public static void testLogBuilders(Path path) throws IOException {
    Assert.assertNotNull(path);
    WritableByteChannel channel = Files.newByteChannel(path,
        StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
    channel.write(ByteBuffer.wrap(LogBuildersTest.class.getName().getBytes(Charset.defaultCharset())));
    channel.close();
    Assert.assertTrue(Files.deleteIfExists(path));
  }
}