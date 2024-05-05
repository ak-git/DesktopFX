package com.ak.util;

import java.io.IOException;
import java.lang.ref.Cleaner;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public enum Clean {
  ;

  public static void clean(Cleaner.Cleanable[] toClean) {
    Arrays.stream(toClean).forEach(Cleaner.Cleanable::clean);
  }

  public static void clean(Path root) {
    Logger.getLogger(Clean.class.getName()).log(Level.INFO, () -> "Clean directory %s".formatted(root.toAbsolutePath()));
    cleanRecursive(root);
    try {
      Files.deleteIfExists(root);
    }
    catch (IOException e) {
      Logger.getLogger(Clean.class.getName()).log(Level.WARNING, root.toString(), e.getMessage());
    }
  }

  private static void cleanRecursive(Path path) {
    if (Files.exists(Objects.requireNonNull(path), LinkOption.NOFOLLOW_LINKS)) {
      try (DirectoryStream<Path> ds = Files.newDirectoryStream(path)) {
        for (Path file : ds) {
          if (Files.isDirectory(file, LinkOption.NOFOLLOW_LINKS)) {
            cleanRecursive(file);
          }
          Files.deleteIfExists(file);
        }
      }
      catch (IOException e) {
        Logger.getLogger(Clean.class.getName()).log(Level.WARNING, path.toString(), e.getMessage());
      }
    }
  }
}
