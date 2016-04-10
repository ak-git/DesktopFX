package com.ak.logging;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
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

  @Test
  public void testBinaryLogBuilder() throws IOException {
    Path path = new BinaryLogBuilder(getClass().getSimpleName(), LocalFileHandler.class).build().getPath();
    WritableByteChannel channel = Files.newByteChannel(path,
        StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
    channel.write(ByteBuffer.wrap(getClass().getName().getBytes()));
    channel.close();
    Files.deleteIfExists(path);
  }

  @AfterClass
  public void tearDown() throws Exception {
    try (DirectoryStream<Path> ds = Files.newDirectoryStream(logPath)) {
      for (Path file : ds) {
        Files.delete(file);
      }
    }
    Files.delete(logPath);
  }
}