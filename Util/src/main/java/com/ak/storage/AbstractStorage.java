package com.ak.storage;

import javax.annotation.Nonnull;

public abstract class AbstractStorage<T> implements Storage<T> {
  @Nonnull
  private final String filePrefix;

  protected AbstractStorage(@Nonnull String filePrefix) {
    if (filePrefix.isEmpty()) {
      throw new IllegalArgumentException();
    }
    this.filePrefix = filePrefix;
  }

  final String getFilePrefix() {
    return filePrefix;
  }
}
