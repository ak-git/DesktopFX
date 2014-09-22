package com.ak.storage;

import java.util.Optional;

public final class GenericStorage<T> extends AbstractStorage<T> {
  private final String fileSuffix;
  private final Class<T> clazz;
  private final T defaultValue;
  private T t;

  private GenericStorage(String fileName, String fileSuffix, Class<T> clazz, T defaultValue) {
    super(fileName);
    this.fileSuffix = fileSuffix;
    this.clazz = clazz;
    this.defaultValue = defaultValue;
  }

  @Override
  public void save(T t) {
    this.t = t;
    LocalStorage.save(t, String.format("%s_%s", fileName(), fileSuffix));
  }

  @Override
  public T load(T defaultValue) {
    if (t == null) {
      t = Optional.ofNullable(defaultValue).orElse(this.defaultValue);
      LocalStorage.load(String.format("%s_%s", fileName(), fileSuffix), clazz, value -> t = value);
    }
    return t;
  }

  public static Storage<String> newStringStorage(String fileName, String fileSuffix) {
    return new GenericStorage<>(fileName, fileSuffix, String.class, "");
  }

  public static Storage<Integer> newIntegerStorage(String fileName, String fileSuffix, int defaultValue) {
    return new GenericStorage<>(fileName, fileSuffix, Integer.class, defaultValue);
  }

  public static Storage<Boolean> newBooleanStorage(String fileName, String fileSuffix, boolean defaultValue) {
    return new GenericStorage<>(fileName, fileSuffix, Boolean.class, defaultValue);
  }
}
