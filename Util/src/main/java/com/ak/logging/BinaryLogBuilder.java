package com.ak.logging;

import java.util.logging.FileHandler;

import javax.annotation.Nonnull;

public final class BinaryLogBuilder extends LogPathBuilder {
  public BinaryLogBuilder(@Nonnull String prefix, @Nonnull Class<? extends FileHandler> fileHandlerClass) {
    super("bin", fileHandlerClass);
    fileName(prefix + localDate(" yyyy-MMM-dd HH-mm-ss"));
  }
}
