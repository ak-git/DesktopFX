package com.ak.storage;

public abstract class AbstractStorage<T> implements Storage<T>, Cloneable {
  private final String fileName;

  protected AbstractStorage(String fileName) {
    if (fileName.isEmpty()) {
      throw new IllegalArgumentException();
    }
    this.fileName = fileName;
  }

  protected final String fileName() {
    return fileName;
  }

  @Override
  protected final Object clone() throws CloneNotSupportedException {
    throw new CloneNotSupportedException();
  }
}
