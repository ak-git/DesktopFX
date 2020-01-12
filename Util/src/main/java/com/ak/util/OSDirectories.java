package com.ak.util;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.ak.util.Strings.EMPTY;

public class OSDirectories {
  public static final String USER_HOME_PATH;

  static {
    PrivilegedAction<String> action = () -> Optional.ofNullable(System.getProperty("user.home")).orElse(EMPTY);
    USER_HOME_PATH = AccessController.doPrivileged(action);
  }

  public static final String VENDOR_ID = Stream.of(OSDirectories.class.getPackage().getName().split("\\.")).limit(2).
      collect(Collectors.joining("."));

  private OSDirectories() {
  }
}
