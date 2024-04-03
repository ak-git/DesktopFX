package com.ak.fx.storage;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.prefs.BackingStoreException;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

final class DoubleArrayStorageTest {
  static Stream<double[]> storage() {
    return Stream.of(new double[] {10.0, 20.1, 30.2, 40.3});
  }

  @ParameterizedTest
  @MethodSource("storage")
  void testSave(double[] values) throws BackingStoreException {
    Storage<double[]> storage = new DoubleArrayStorage(DoubleArrayStorageTest.class, "#%08x".formatted(hashCode()));
    storage.save(values);
    assertThat(storage.get()).contains(values);
    storage.delete();
    assertThat(storage.get()).isEmpty();
  }

  @ParameterizedTest
  @MethodSource("storage")
  void testUpdate(double[] rectangle) {
    Storage<double[]> storage = new DoubleArrayStorage(DoubleArrayStorageTest.class, "#%08x".formatted(hashCode()));
    assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> storage.update(rectangle));
  }
}