package com.ak.file;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import com.ak.logging.OutputBuilders;
import com.ak.util.Clean;
import com.ak.util.Extension;
import com.ak.util.Strings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class RecursiveWatcherTest {
  @Nonnull
  private final Path path;

  RecursiveWatcherTest() throws IOException {
    path = OutputBuilders.NONE.build(Strings.EMPTY).getPath();
  }

  @AfterEach
  public void cleanUp() {
    Clean.clean(path);
  }

  @Test
  void test() throws IOException, InterruptedException {
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