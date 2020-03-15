package com.ak.logging;

import com.ak.util.Extensions;
import com.ak.util.LocalIO;

public enum LoggingBuilder {
  LOGGING;

  public final LocalIO build(String path) {
    return new LogPathBuilder().addPath(path).fileName(fileName()).build();
  }

  public final String fileName() {
    return Extensions.PROPERTIES.attachTo(name().toLowerCase());
  }
}
