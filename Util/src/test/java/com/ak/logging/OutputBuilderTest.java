package com.ak.logging;

import com.ak.util.Extension;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class OutputBuilderTest {
  @Test
  void testLocalFileHandler() throws IOException {
    Path txt = new OutputBuilder(Extension.TXT).fileNameWithDateTime(OutputBuilderTest.class.getSimpleName()).build().getPath();
    assertTrue(Files.exists(Files.createFile(txt)));
    assertTrue(Files.deleteIfExists(txt));
  }
}