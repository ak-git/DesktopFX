package com.ak.logging;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import javax.annotation.Nonnull;

import com.ak.util.Clean;
import com.ak.util.Extension;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.testng.log.TextFormatter;

public class LocalFileHandlerTest {
  @Nonnull
  private final Path logPath;

  public LocalFileHandlerTest() throws IOException {
    logPath = new LogPathBuilder(Extension.NONE, LocalFileHandler.class).addPath("testSubDir").build().getPath().getParent();
  }

  @BeforeSuite
  @AfterSuite
  public void setUp() {
    Clean.clean(logPath);
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
}