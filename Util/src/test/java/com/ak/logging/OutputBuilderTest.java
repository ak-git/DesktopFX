package com.ak.logging;

import com.ak.util.Extension;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class OutputBuilderTest {
  @Test
  void localFileHandler() throws Exception {
    Path txt = new OutputBuilder(Extension.TXT).fileNameWithDateTime(OutputBuilderTest.class.getSimpleName()).build().getPath();
    assertThat(Files.createFile(txt)).exists();
    assertThat(Files.deleteIfExists(txt)).isTrue();
  }
}