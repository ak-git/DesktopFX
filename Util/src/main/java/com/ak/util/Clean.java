package com.ak.util;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

public enum Clean {
  ;

  public static void clean(@Nonnull Path path) {
    Logger.getLogger(Clean.class.getName()).log(Level.INFO, () -> "Clean directory %s".formatted(path));
    try (DirectoryStream<Path> ds = Files.newDirectoryStream(path, Files::isRegularFile)) {
      for (Path file : ds) {
        Files.deleteIfExists(file);
      }
    }
    catch (IOException ex) {
      Logger.getLogger(Clean.class.getName()).log(Level.WARNING, path.toString(), ex.getMessage());
    }
  }
}
