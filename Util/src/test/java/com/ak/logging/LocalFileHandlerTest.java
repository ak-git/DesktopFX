package com.ak.logging;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

import javax.annotation.Nonnull;

import com.ak.util.Clean;
import com.ak.util.Extension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LocalFileHandlerTest {
  @Nonnull
  private final Path logPath;

  LocalFileHandlerTest() throws IOException {
    logPath = new LogPathBuilder(Extension.NONE, LocalFileHandler.class).addPath("testSubDir").build().getPath().getParent();
  }

  @BeforeEach
  @AfterEach
  public void setUp() {
    Clean.clean(logPath);
  }

  @Test
  void testLocalFileHandler() throws IOException {
    LocalFileHandler handler = new LocalFileHandler();
    handler.setFormatter(new SimpleFormatter());
    handler.publish(new LogRecord(Level.ALL, LocalFileHandler.class.getName()));
    handler.close();

    try (DirectoryStream<Path> ds = Files.newDirectoryStream(logPath, "*.log")) {
      int count = 0;
      for (Path file : ds) {
        List<String> strings = Files.readAllLines(file);
        assertThat(strings).hasSize(2);
        assertThat(strings.get(1)).contains(LocalFileHandler.class.getName());
        count++;
      }
      assertThat(count).withFailMessage("Must be the only one .log file in " + logPath).isEqualTo(1);
    }
  }
}