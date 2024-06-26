package com.ak.fx.storage;

import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

final class BoundsStorage extends AbstractStorage<Rectangle2D.Double> {
  private static final String BOUNDS_X = "boundsX";
  private static final String BOUNDS_Y = "boundsY";
  private static final String BOUNDS_WIDTH = "boundsWidth";
  private static final String BOUNDS_HEIGHT = "boundsHeight";

  BoundsStorage(Class<?> c, String nodeName) {
    super(c, nodeName);
  }

  @Override
  public void save(Rectangle2D.Double rectangle) {
    preferences().putDouble(BOUNDS_X, rectangle.getX());
    preferences().putDouble(BOUNDS_Y, rectangle.getY());
    preferences().putDouble(BOUNDS_WIDTH, rectangle.getWidth());
    preferences().putDouble(BOUNDS_HEIGHT, rectangle.getHeight());
  }

  @Override
  public void update(Rectangle2D.Double rectangle) {
    throw new UnsupportedOperationException(rectangle.toString());
  }

  @Override
  public Optional<Rectangle2D.Double> get() {
    double[] doubles = Stream.of(BOUNDS_X, BOUNDS_Y, BOUNDS_WIDTH, BOUNDS_HEIGHT)
        .mapToDouble(key -> preferences().getDouble(key, Double.NaN)).toArray();
    if (Arrays.stream(doubles).noneMatch(Double::isNaN)) {
      return Optional.of(new Rectangle2D.Double(doubles[0], doubles[1], doubles[2], doubles[3]));
    }
    else {
      return Optional.empty();
    }
  }
}
