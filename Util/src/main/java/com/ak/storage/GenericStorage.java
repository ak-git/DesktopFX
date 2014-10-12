package com.ak.storage;

import java.util.Optional;

public final class GenericStorage<T> extends AbstractStorage<T> {
  private final String fileSuffix;
  private final Class<T> clazz;
  private final T defaultValue;
  private T t;

  private GenericStorage(String filePrefix, String fileSuffix, Class<T> clazz, T defaultValue) {
    super(filePrefix);
    this.fileSuffix = fileSuffix;
    this.clazz = clazz;
    this.defaultValue = defaultValue;
  }

  @Override
  public void save(T t) {
    this.t = t;
    LocalStorage.save(t, fileName(fileSuffix));
  }

  @Override
  public T load(T defaultValue) {
    if (t == null) {
      t = Optional.ofNullable(defaultValue).orElse(this.defaultValue);
      LocalStorage.load(fileName(fileSuffix), clazz, value -> t = value);
    }
    return t;
  }

  public static Storage<String> newStringStorage(String filePrefix, String fileSuffix) {
    return new GenericStorage<>(filePrefix, fileSuffix, String.class, "");
  }

  public static Storage<Integer> newIntegerStorage(String filePrefix, String fileSuffix, int defaultValue) {
    return new GenericStorage<>(filePrefix, fileSuffix, Integer.class, defaultValue);
  }

  public static Storage<Boolean> newBooleanStorage(String filePrefix, String fileSuffix) {
    return new GenericStorage<>(filePrefix, fileSuffix, Boolean.class, Boolean.FALSE);
  }
}
