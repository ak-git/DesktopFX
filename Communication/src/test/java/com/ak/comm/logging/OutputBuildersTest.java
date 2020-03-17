package com.ak.comm.logging;

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

public class OutputBuildersTest {
  private static final Object[][] EMPTY_OBJECTS = {};

  @DataProvider(name = "outBuilders")
  public static Object[][] outBuilders() {
    return Stream.of(OutputBuilders.values()).
        map(outBuilder -> {
          try {
            return new Object[] {OutputBuilders.build("02f29f760fa69e6c404c03de0f1e15f9").getPath()};
          }
          catch (IOException e) {
            return new Object[] {null};
          }
        }).collect(Collectors.toList()).toArray(EMPTY_OBJECTS);
  }


  @Test(dataProvider = "outBuilders")
  public void testOutBuilders(Path path) throws IOException {
    Assert.assertNotNull(path);
    WritableByteChannel channel = Files.newByteChannel(path,
        StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
    channel.write(ByteBuffer.wrap(OutputBuildersTest.class.getName().getBytes(Charset.defaultCharset())));
    channel.close();
    Files.deleteIfExists(path);
  }
}