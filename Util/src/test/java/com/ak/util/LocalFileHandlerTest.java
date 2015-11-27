package com.ak.util;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.log.TextFormatter;

public class LocalFileHandlerTest {
  private LocalFileHandlerTest() {
  }

  @Test
  public void testLocalFileHandler() throws IOException {
    Path logPath = new LocalFileIO.LogBuilder().
        addPath(LocalFileHandler.class.getSimpleName()).addPath("testSubDir").build().getPath();

    try (DirectoryStream<Path> ds = Files.newDirectoryStream(logPath)) {
      for (Path file : ds) {
        Files.delete(file);
      }
      Files.delete(logPath);
    }

    LocalFileHandler handler = new LocalFileHandler();
    handler.setFormatter(new TextFormatter());
    handler.publish(new LogRecord(Level.ALL, LocalFileHandler.class.getName()));

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

    try (DirectoryStream<Path> ds = Files.newDirectoryStream(logPath)) {
      for (Path file : ds) {
        Files.delete(file);
      }
      Files.delete(logPath);
    }
  }
}