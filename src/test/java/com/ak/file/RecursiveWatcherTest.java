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
import org.testng.annotations.AfterSuite;
import org.testng.annotations.Test;

public class RecursiveWatcherTest {
  @Nonnull
  private final Path path;

  public RecursiveWatcherTest() throws IOException {
    path = OutputBuilders.NONE.build(Strings.EMPTY).getPath();
  }

  @AfterSuite
  public void setUp() {
    Clean.clean(path);
  }

  @Test
  public void test() throws IOException, InterruptedException {
    Files.createDirectories(path);
    Path subDir = Files.createTempDirectory(path, Strings.EMPTY);
    CountDownLatch latch = new CountDownLatch(2);
    Closeable watcher = new RecursiveWatcher(path, p -> latch.countDown(), Extension.TXT);
    while (!latch.await(2, TimeUnit.SECONDS)) {
      Files.createTempFile(Files.createTempDirectory(subDir, Strings.EMPTY), Strings.EMPTY, Extension.TXT.attachTo(Strings.EMPTY));
    }
    watcher.close();
  }
}