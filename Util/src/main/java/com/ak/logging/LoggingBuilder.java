package com.ak.logging;

import com.ak.util.LocalIO;
import com.ak.util.PropertiesSupport;

public enum LoggingBuilder {
  LOGGING;

  public final LocalIO build(String path) {
    return new LogPathBuilder().addPath(path).fileName(fileName()).build();
  }

  public final String fileName() {
    return PropertiesSupport.addExtension(name().toLowerCase());
  }
}
