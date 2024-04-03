package com.ak.fx.storage;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.prefs.BackingStoreException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

final class StringStorageTest {
  private final Storage<String> storage = new StringStorage(StringStorageTest.class, "#%08x".formatted(hashCode()));

  @ParameterizedTest
  @ValueSource(strings = "Something String")
  void testSave(String value) throws BackingStoreException {
    storage.save(value);
    assertThat(storage.get()).contains(value);
    storage.delete();
    assertThat(storage.get()).isEmpty();
  }

  @ParameterizedTest
  @EmptySource
  void testUpdate(String value) {
    assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> storage.update(value));
  }
}