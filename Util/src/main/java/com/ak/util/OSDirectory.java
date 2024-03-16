package com.ak.util;

import java.nio.file.Path;

@FunctionalInterface
public interface OSDirectory {
  Path getDirectory();

  static <E extends Enum<E> & OSDirectory> OSDirectory of(Class<E> enumClass) {
    return new OSDirectory() {
      private final Path path = Enum.valueOf(enumClass, OS.get().name()).getDirectory();

      @Override
      public Path getDirectory() {
        return path;
      }
    };
  }
}
