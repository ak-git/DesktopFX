package com.ak.file;

import com.ak.logging.OutputBuilders;
import com.ak.util.Clean;
import com.ak.util.Extension;
import com.ak.util.Strings;
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
      PATH = OutputBuilders.NONE.build(Strings.EMPTY).getPath();
    }
    catch (IOException e) {
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
    Closeable watcher = new RecursiveWatcher(path, p -> latch.countDown(), Extension.TXT);
    while (!latch.await(2, TimeUnit.SECONDS)) {
      Files.createTempFile(Files.createTempDirectory(subDir, Strings.EMPTY), Strings.EMPTY, Extension.TXT.attachTo(Strings.EMPTY));
    }
    watcher.close();
  }
}