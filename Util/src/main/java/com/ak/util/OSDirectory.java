package com.ak.util;

import java.nio.file.Path;

@FunctionalInterface
public interface OSDirectory {
  Path getDirectory();
}
