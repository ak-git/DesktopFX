package com.ak.logging;

import javax.annotation.Nonnull;

public final class BinaryLogBuilder extends LogPathBuilder {
  public BinaryLogBuilder(@Nonnull String prefix) {
    super("bin", LocalFileHandler.class);
    fileName(prefix + localDate(" yyyy-MMM-dd HH-mm-ss"));
  }
}
