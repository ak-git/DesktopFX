package com.ak.file;

import com.ak.util.Extension;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class RecursiveWatcher implements Closeable {
  private final ExecutorService service = Executors.newSingleThreadExecutor();
  private final WatchService watchService;
  private final Map<WatchKey, Path> directories = new HashMap<>();
  private final String glob;

  public RecursiveWatcher(Path start, Consumer<Path> doSome, Extension extension) throws IOException {
    watchService = FileSystems.getDefault().newWatchService();
    glob = extension.attachTo("*");

    BiConsumer<Path, Consumer<Path>> register = (child, consumer) -> {
      if (Files.isDirectory(child, LinkOption.NOFOLLOW_LINKS)) {
        registerTree(child, doSome);
      }
      else if (Files.isRegularFile(child, LinkOption.NOFOLLOW_LINKS) && extension.is(child.toString())) {
        consumer.accept(child);
      }
    };

    service.execute(() -> {
      try (watchService) {
        registerTree(start, doSome);
        for (WatchKey key = watchService.take(); key != null && !directories.isEmpty(); key = watchService.take()) {
          WatchKey finalKey = key;
          key.pollEvents().stream()
              .filter(watchEvent -> StandardWatchEventKinds.ENTRY_CREATE.equals(watchEvent.kind()))
              .map(WatchEvent::context).map(Path.class::cast)
              .map(path -> directories.get(finalKey).resolve(path))
              .mapMulti(register)
              .forEach(doSome);

          if (!key.reset()) {
            directories.remove(key);
          }
        }
      }
      catch (InterruptedException | ClosedWatchServiceException e) {
        Thread.currentThread().interrupt();
        Logger.getLogger(getClass().getName()).log(Level.CONFIG, e.getMessage(), e);
      }
      catch (IOException e) {
        Logger.getLogger(getClass().getName()).log(Level.WARNING, e.getMessage(), e);
      }
    });
  }

  @Override
  public void close() throws IOException {
    try {
      watchService.close();
    }
    finally {
      service.shutdownNow();
    }
  }

  private void registerTree(Path start, Consumer<Path> doSome) {
    try {
      Files.walkFileTree(start, new SimpleFileVisitor<>() {
        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
          directories.put(dir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE), dir);
          try (DirectoryStream<Path> files = Files.newDirectoryStream(dir, glob)) {
            files.forEach(doSome);
          }
          catch (IOException e) {
            Logger.getLogger(getClass().getName()).log(Level.WARNING, e.getMessage(), e);
          }
          return FileVisitResult.CONTINUE;
        }
      });
    }
    catch (IOException e) {
      Logger.getLogger(getClass().getName()).log(Level.WARNING, e.getMessage(), e);
    }
  }
}
