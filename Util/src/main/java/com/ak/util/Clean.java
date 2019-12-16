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
    if (!PropertiesSupport.CACHE.check()) {
      Arrays.stream(toClean).forEach(Cleaner.Cleanable::clean);
    }
  }

  public static void clean(@Nonnull Path path) {
    try (DirectoryStream<Path> ds = Files.newDirectoryStream(path, entry -> Files.isRegularFile(entry))) {
      for (Path file : ds) {
        Files.deleteIfExists(file);
      }
    }
    catch (IOException ex) {
      Logger.getLogger(Clean.class.getName()).log(Level.WARNING, path.toString(), ex.getMessage());
    }
  }
}
