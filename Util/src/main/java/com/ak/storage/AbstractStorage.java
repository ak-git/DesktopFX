package com.ak.storage;

public abstract class AbstractStorage<T> implements Storage<T>, Cloneable {
  private final String filePrefix;

  public AbstractStorage(String filePrefix) {
    if (filePrefix.isEmpty()) {
      throw new IllegalArgumentException();
    }
    this.filePrefix = filePrefix;
  }

  final String getFilePrefix() {
    return filePrefix;
  }

  @Override
  protected final Object clone() throws CloneNotSupportedException {
    throw new CloneNotSupportedException();
  }
}
