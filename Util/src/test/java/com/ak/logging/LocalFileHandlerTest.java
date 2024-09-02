package com.ak.logging;

import com.ak.util.Clean;
import com.ak.util.Extension;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

class LocalFileHandlerTest {
  private static final Path PATH;

  static {
    try {
      PATH = new LogPathBuilder(Extension.NONE, LocalFileHandler.class).addPath("testSubDir").build().getPath().getParent();
    }
    catch (IOException e) {
      fail(e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }

  @BeforeAll
  @AfterAll
  static void cleanUp() {
    Clean.clean(Objects.requireNonNull(PATH));
  }

  @Test
  void testLocalFileHandler() throws IOException {
    LocalFileHandler handler = new LocalFileHandler();
    handler.setFormatter(new SimpleFormatter());
    handler.publish(new LogRecord(Level.ALL, LocalFileHandler.class.getName()));
    handler.close();

    Path logPath = Objects.requireNonNull(PATH);
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