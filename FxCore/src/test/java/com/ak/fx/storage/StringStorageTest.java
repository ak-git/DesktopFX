package com.ak.fx.storage;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.throwable;
import static org.junit.jupiter.api.Assertions.assertAll;

final class StringStorageTest {
  private final Storage<String> storage = new StringStorage(StringStorageTest.class, "#%08x".formatted(hashCode()));

  @ParameterizedTest
  @ValueSource(strings = "Something String")
  void save(String value) throws Exception {
    storage.save(value);
    assertAll(storage.toString(),
        () -> assertThat(storage.toString()).contains(StringStorage.class.getSimpleName()).contains("preferences"),
        () -> assertThat(storage.get()).hasValue(value)
    );
    storage.delete();
    assertThat(storage.get()).isEmpty();
  }

  @ParameterizedTest
  @EmptySource
  void update(String value) {
    assertThatThrownBy(() -> storage.update(value)).asInstanceOf(throwable(UnsupportedOperationException.class))
        .hasNoCause();
  }
}