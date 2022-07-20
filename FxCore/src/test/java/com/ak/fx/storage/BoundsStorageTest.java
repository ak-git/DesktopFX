package com.ak.fx.storage;

import java.awt.geom.Rectangle2D;
import java.util.prefs.BackingStoreException;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertNull;

final class BoundsStorageTest {
  static Stream<Rectangle2D.Double> storage() {
    return Stream.of(new Rectangle2D.Double(10, 20, 30, 40));
  }

  @ParameterizedTest
  @MethodSource("storage")
  void testSave(@Nonnull Rectangle2D.Double rectangle) throws BackingStoreException {
    Storage<Rectangle2D.Double> storage = new BoundsStorage(BoundsStorageTest.class, "%08x".formatted(hashCode()));
    storage.save(rectangle);
    assertThat(storage.get()).isEqualTo(rectangle);
    storage.delete();
    assertNull(storage.get());
  }

  @ParameterizedTest
  @MethodSource("storage")
  void testUpdate(@Nonnull Rectangle2D.Double rectangle) {
    Storage<Rectangle2D.Double> storage = new BoundsStorage(BoundsStorageTest.class, "%08x".formatted(hashCode()));
    assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> storage.update(rectangle));
  }
}