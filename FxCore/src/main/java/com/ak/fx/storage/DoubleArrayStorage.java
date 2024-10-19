package com.ak.fx.storage;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.DoublePredicate;
import java.util.stream.IntStream;

public final class DoubleArrayStorage extends AbstractStorage<double[]> {
  private static final String KEY = "dValue";

  public DoubleArrayStorage(Class<?> c, String nodeName) {
    super(c, nodeName);
  }

  @Override
  public void save(double[] values) {
    for (int i = 0; i < values.length; i++) {
      preferences().putDouble("%s%d".formatted(KEY, i), values[i]);
    }
  }

  @Override
  public void update(double[] values) {
    throw new UnsupportedOperationException(Arrays.toString(values));
  }

  @Override
  public Optional<double[]> get() {
    double[] array = IntStream.iterate(0, i -> i + 1)
        .mapToDouble(i -> preferences().getDouble("%s%d".formatted(KEY, i), Double.NaN))
        .takeWhile(((DoublePredicate) Double::isNaN).negate())
        .toArray();
    return array.length == 0 ? Optional.empty() : Optional.of(array);
  }
}
