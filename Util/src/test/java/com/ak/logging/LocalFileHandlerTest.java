package com.ak.logging;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.log.TextFormatter;

public class LocalFileHandlerTest {
  private Path logPath;

  private LocalFileHandlerTest() {
  }

  @BeforeClass
  public void setUp() throws Exception {
    logPath = new LogPathBuilder().addPath(LocalFileHandler.class.getSimpleName()).addPath("testSubDir").
        build().getPath().getParent();
    tearDown();
  }

  @Test
  public void testLocalFileHandler() throws IOException {
    LocalFileHandler handler = new LocalFileHandler();
    handler.setFormatter(new TextFormatter());
    handler.publish(new LogRecord(Level.ALL, LocalFileHandler.class.getName()));
    handler.close();

    try (DirectoryStream<Path> ds = Files.newDirectoryStream(logPath, "*.log")) {
      int count = 0;
      for (Path file : ds) {
        List<String> strings = Files.readAllLines(file);
        Assert.assertEquals(strings.size(), 1);
        Assert.assertEquals(strings.get(0), LocalFileHandler.class.getName());
        count++;
      }
      Assert.assertEquals(count, 1, "Must be the only one .log file in " + logPath);
    }
  }

  @DataProvider(name = "logBuilders")
  public static Object[][] logBuilders() throws IOException {
    return new Object[][] {
        {new BinaryLogBuilder(LocalFileHandlerTest.class.getSimpleName()).build().getPath()},
        {new BinaryLogBuilder("02f29f660fa69e6c404c03de0f1e15f9").build().getPath()},
    };
  }


  @Test(dataProvider = "logBuilders")
  public void testLogBuilders(Path path) throws IOException {
    WritableByteChannel channel = Files.newByteChannel(path,
        StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
    channel.write(ByteBuffer.wrap(getClass().getName().getBytes(Charset.defaultCharset())));
    channel.close();
    Files.deleteIfExists(path);
  }

  @AfterSuite
  public void tearDown() throws Exception {
    try (DirectoryStream<Path> ds = Files.newDirectoryStream(logPath)) {
      for (Path file : ds) {
        Files.delete(file);
      }
    }
    Files.delete(logPath);
  }
}