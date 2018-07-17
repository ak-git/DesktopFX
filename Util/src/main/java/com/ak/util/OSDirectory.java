package com.ak.util;

import java.nio.file.Path;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Optional;

import static com.ak.util.Strings.EMPTY;

public interface OSDirectory {
  String USER_HOME_PATH = AccessController.doPrivileged(
      (PrivilegedAction<String>) () -> Optional.ofNullable(System.getProperty("user.home")).orElse(EMPTY));

  Path getDirectory();
}
