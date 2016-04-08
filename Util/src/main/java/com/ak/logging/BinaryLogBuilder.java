package com.ak.logging;

import java.util.logging.FileHandler;

public final class BinaryLogBuilder extends LogPathBuilder {
  public BinaryLogBuilder(String prefix, Class<? extends FileHandler> fileHandlerClass) {
    super("bin", fileHandlerClass);
    fileName(prefix + localDate(" yyyy-MMM-dd HH-mm-ss"));
  }
}
