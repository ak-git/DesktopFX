package com.ak.logging;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import javax.annotation.Nonnull;

import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.testng.log.TextFormatter;

public class LocalFileHandlerTest {
  @Nonnull
  private final Path logPath;

  private LocalFileHandlerTest() throws IOException {
    logPath = new LogPathBuilder().addPath(LocalFileHandler.class.getSimpleName()).addPath("testSubDir").
        build().getPath().getParent();
  }

  @BeforeSuite
  @AfterSuite
  public void setUp() throws Exception {
    delete(logPath);
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

  static void delete(@Nonnull Path root) throws Exception {
    try (DirectoryStream<Path> ds = Files.newDirectoryStream(root)) {
      for (Path file : ds) {
        if (Files.isDirectory(file)) {
          delete(file);
        }
        else {
          Files.delete(file);
        }
      }
    }
    Files.delete(root);
  }
}