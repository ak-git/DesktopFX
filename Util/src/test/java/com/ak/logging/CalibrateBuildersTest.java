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

public class CalibrateBuildersTest {
  private static final Object[][] EMPTY_OBJECTS = {};

  private CalibrateBuildersTest() {
  }

  @DataProvider(name = "builders")
  public static Object[][] builders() {
    return Stream.of(CalibrateBuilders.values()).
        map(builder -> {
          try {
            return new Object[] {builder.build("c404c03de0f1e15f902f29f660fa69e6").getPath()};
          }
          catch (IOException e) {
            return new Object[] {null};
          }
        }).collect(Collectors.toList()).toArray(EMPTY_OBJECTS);
  }

  @Test(dataProvider = "builders")
  public static void testBuild(Path path) throws IOException {
    Assert.assertNotNull(path);
    WritableByteChannel channel = Files.newByteChannel(path,
        StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
    channel.write(ByteBuffer.wrap(LogBuildersTest.class.getName().getBytes(Charset.defaultCharset())));
    channel.close();
    Assert.assertTrue(Files.deleteIfExists(path));
  }
}