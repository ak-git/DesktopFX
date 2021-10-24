package com.ak.util;

import java.io.IOException;
import java.lang.ref.Cleaner;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

public enum Clean {
  ;

  public static void clean(@Nonnull Cleaner.Cleanable[] toClean) {
    Arrays.stream(toClean).forEach(Cleaner.Cleanable::clean);
  }

  public static void clean(@Nonnull Path root) {
    Logger.getLogger(Clean.class.getName()).log(Level.INFO, () -> "Clean directory %s".formatted(root));
    try (DirectoryStream<Path> ds = Files.newDirectoryStream(root)) {
      for (Path file : ds) {
        if (Files.isDirectory(file)) {
          clean(file);
        }
        Files.deleteIfExists(file);
      }
      Files.deleteIfExists(root);
    }
    catch (IOException e) {
      Logger.getLogger(Clean.class.getName()).log(Level.WARNING, root.toString(), e.getMessage());
    }
  }
}
