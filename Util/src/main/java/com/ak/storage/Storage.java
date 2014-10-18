package com.ak.storage;

public interface Storage<T> {
  void save(T t);

  void update(T t);

  T get();
}
