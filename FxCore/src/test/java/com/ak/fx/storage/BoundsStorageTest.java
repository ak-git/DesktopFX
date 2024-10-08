package com.ak.fx.storage;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.awt.geom.Rectangle2D;
import java.util.prefs.BackingStoreException;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

final class BoundsStorageTest {
  static Stream<Rectangle2D.Double> storage() {
    return Stream.of(new Rectangle2D.Double(10, 20, 30, 40));
  }

  @ParameterizedTest
  @MethodSource("storage")
  void testSave(Rectangle2D.Double rectangle) throws BackingStoreException {
    Storage<Rectangle2D.Double> storage = new BoundsStorage(BoundsStorageTest.class, "#%08x".formatted(hashCode()));
    storage.save(rectangle);
    Assertions.assertAll(storage.toString(),
        () -> assertThat(storage.toString()).contains(BoundsStorage.class.getSimpleName()).contains("preferences"),
        () -> assertThat(storage.get()).contains(rectangle)
    );
    storage.delete();
    assertThat(storage.get()).isEmpty();
  }

  @ParameterizedTest
  @MethodSource("storage")
  void testUpdate(Rectangle2D.Double rectangle) {
    Storage<Rectangle2D.Double> storage = new BoundsStorage(BoundsStorageTest.class, "#%08x".formatted(hashCode()));
    assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> storage.update(rectangle));
  }
}