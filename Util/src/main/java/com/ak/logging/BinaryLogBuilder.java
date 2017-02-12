package com.ak.logging;

public final class BinaryLogBuilder extends LogPathBuilder {
  public BinaryLogBuilder() {
    super("bin", LocalFileHandler.class);
  }
}
