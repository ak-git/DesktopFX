package com.ak.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import static com.ak.util.Strings.EMPTY;

public enum OSDirectories {
  ;

  private static final String USER_HOME_PATH;

  static {
    PrivilegedAction<String> action = () -> Optional.ofNullable(System.getProperty("user.home")).orElse(EMPTY);
    USER_HOME_PATH = AccessController.doPrivileged(action);
  }

  public static final String VENDOR_ID = Stream.of(OSDirectories.class.getPackage().getName().split("\\.")).limit(2).
      collect(Collectors.joining("."));

  @Nonnull
  public static Path getDirectory(@Nonnull String... candidates) {
    return Arrays.stream(candidates)
        .map(p -> Paths.get(USER_HOME_PATH).resolve(p))
        .filter(p -> Files.isDirectory(p) && Files.isWritable(p) && Files.exists(p))
        .findFirst()
        .orElse(Paths.get(EMPTY));
  }
}
