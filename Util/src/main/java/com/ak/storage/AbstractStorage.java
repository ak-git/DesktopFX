package com.ak.storage;

abstract class AbstractStorage<T> implements Storage<T>, Cloneable {
  private final String filePrefix;

  AbstractStorage(String filePrefix) {
    if (filePrefix.isEmpty()) {
      throw new IllegalArgumentException();
    }
    this.filePrefix = filePrefix;
  }

  final String fileName(String fileSuffix) {
    return String.format("%s_%s", filePrefix, fileSuffix);
  }

  @Override
  protected final Object clone() throws CloneNotSupportedException {
    throw new CloneNotSupportedException();
  }
}
