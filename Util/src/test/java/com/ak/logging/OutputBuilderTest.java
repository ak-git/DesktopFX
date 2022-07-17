package com.ak.logging;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.StreamSupport;

import javax.annotation.Nonnull;

import com.ak.util.Clean;
import com.ak.util.Extension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class OutputBuilderTest {
  @Nonnull
  private final Path outPath;

  OutputBuilderTest() throws IOException {
    Path txt = new OutputBuilder(Extension.TXT).fileNameWithDateTime(OutputBuilderTest.class.getSimpleName()).build().getPath();
    Files.createFile(txt);
    outPath = txt.getParent();
  }

  @AfterEach
  void setUp() {
    Clean.clean(outPath);
  }

  @Test
  void testLocalFileHandler() throws IOException {
    try (DirectoryStream<Path> paths = Files.newDirectoryStream(outPath, "*.txt")) {
      assertTrue(StreamSupport.stream(paths.spliterator(), true).findAny().isPresent(), outPath::toString);
    }
  }
}