package com.ak.storage;

abstract class AbstractStorage<T> implements Storage<T>, Cloneable {
  private final String fileName;

  AbstractStorage(String fileName) {
    if (fileName.isEmpty()) {
      throw new IllegalArgumentException();
    }
    this.fileName = fileName;
  }

  final String fileName() {
    return fileName;
  }

  @Override
  protected final Object clone() throws CloneNotSupportedException {
    throw new CloneNotSupportedException();
  }
}
