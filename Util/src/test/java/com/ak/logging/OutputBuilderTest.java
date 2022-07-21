package com.ak.logging;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.stream.StreamSupport;

import javax.annotation.Nullable;

import com.ak.util.Clean;
import com.ak.util.Extension;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OutputBuilderTest {
  @Nullable
  private static Path PATH;

  static {
    try {
      Path txt = new OutputBuilder(Extension.TXT).fileNameWithDateTime(OutputBuilderTest.class.getSimpleName()).build().getPath();
      Files.createFile(txt);
      PATH = txt.getParent();
    }
    catch (IOException e) {
      fail(e.getMessage(), e);
    }
  }

  @AfterAll
  static void cleanUp() {
    Clean.clean(Objects.requireNonNull(PATH));
  }

  @Test
  void testLocalFileHandler() throws IOException {
    Path outPath = Objects.requireNonNull(PATH);
    try (DirectoryStream<Path> paths = Files.newDirectoryStream(outPath, "*.txt")) {
      assertTrue(StreamSupport.stream(paths.spliterator(), true).findAny().isPresent(), outPath::toString);
    }
  }
}