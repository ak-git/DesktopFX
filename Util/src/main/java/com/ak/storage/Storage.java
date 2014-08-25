package com.ak.storage;

public interface Storage<T> {
  void save(T t);

  T load(T t);
}
