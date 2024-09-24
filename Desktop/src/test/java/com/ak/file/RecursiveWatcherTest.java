package com.ak.file;

import com.ak.util.Clean;
import com.ak.util.Extension;
import com.ak.util.Strings;
import com.ak.util.UIConstants;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class RecursiveWatcherTest {
  private static final Path PATH;

  static {
    try {
      PATH = Files.createTempDirectory("test %s.".formatted(RecursiveWatcherTest.class.getPackageName()));
    }
    catch (IOException e) {
      Assertions.fail(e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }

  @AfterAll
  static void cleanUp() {
    Clean.clean(Objects.requireNonNull(PATH));
  }

  @Test
  void test() throws IOException, InterruptedException {
    Path path = Objects.requireNonNull(PATH);
    assertNotNull(Files.createTempFile(Files.createDirectories(path), Strings.EMPTY, Extension.TXT.attachTo(Strings.EMPTY)));
    Path subDir = Files.createTempDirectory(path, Strings.EMPTY);
    assertNotNull(subDir, path::toString);
    CountDownLatch latch = new CountDownLatch(2);
    try (Closeable ignoreWatcher = new RecursiveWatcher(path, ignore -> latch.countDown(), Extension.TXT)) {
      while (!latch.await(UIConstants.UI_DELAY.getSeconds(), TimeUnit.SECONDS)) {
        Files.createTempFile(Files.createTempDirectory(subDir, Strings.EMPTY), Strings.EMPTY, Extension.TXT.attachTo(Strings.EMPTY));
      }
    }
  }
}