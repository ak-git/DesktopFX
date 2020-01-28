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

import com.ak.util.Strings;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class LoggingBuilderTest {
  private static final Object[][] EMPTY_OBJECTS = {};

  @DataProvider(name = "logBuilders")
  public static Object[][] logBuilders() {
    return Stream.of(LoggingBuilder.values()).
        map(builder -> {
          try {
            return new Object[] {builder.build(Strings.EMPTY).getPath()};
          }
          catch (IOException e) {
            return new Object[] {null};
          }
        }).collect(Collectors.toList()).toArray(EMPTY_OBJECTS);
  }


  @Test(dataProvider = "logBuilders")
  public void testBuild(Path path) throws IOException {
    Assert.assertNotNull(path);
    WritableByteChannel channel = Files.newByteChannel(path,
        StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
    channel.write(ByteBuffer.wrap(LogBuildersTest.class.getName().getBytes(Charset.defaultCharset())));
    channel.close();
    Assert.assertTrue(Files.deleteIfExists(path));
  }

  @Test
  public void testFileName() {
    Stream.of(LoggingBuilder.values()).forEach(loggingBuilder -> {
      Assert.assertTrue(loggingBuilder.fileName().startsWith(loggingBuilder.name().toLowerCase()));
      Assert.assertTrue(loggingBuilder.fileName().endsWith(".properties"));
    });
  }
}