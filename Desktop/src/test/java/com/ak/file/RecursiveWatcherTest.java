package com.ak.file;

import com.ak.util.Extension;
import com.ak.util.Strings;
import com.ak.util.UIConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class RecursiveWatcherTest {
  @Test
  void test(@TempDir Path path) throws IOException, InterruptedException {
    assertNotNull(Files.createTempFile(Files.createDirectories(path), Strings.EMPTY, Extension.TXT.attachTo(Strings.EMPTY)));
    Path subDir = Files.createTempDirectory(path, Strings.EMPTY);
    assertNotNull(subDir, path::toString);
    CountDownLatch latch = new CountDownLatch(2);
    try (var _ = new RecursiveWatcher(path, _ -> latch.countDown(), Extension.TXT)) {
      while (!latch.await(UIConstants.UI_DELAY.getSeconds(), TimeUnit.SECONDS)) {
        Files.createTempFile(Files.createTempDirectory(subDir, Strings.EMPTY), Strings.EMPTY, Extension.TXT.attachTo(Strings.EMPTY));
      }
    }
  }
}