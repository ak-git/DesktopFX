package com.ak.rsm.relative;

import com.ak.util.Strings;

public abstract sealed class Layer1RelativeMedium implements RelativeMediumLayers {
  public static final RelativeMediumLayers SINGLE_LAYER = new SingleLayer();
  public static final RelativeMediumLayers NAN = new NaNLayer();

  @Override
  public final double hToL() {
    return 0.0;
  }

  private static final class SingleLayer extends Layer1RelativeMedium {
    @Override
    public double k12() {
      return 0;
    }

    public String toString() {
      return Strings.EMPTY;
    }
  }

  private static final class NaNLayer extends Layer1RelativeMedium {
    @Override
    public double k12() {
      return Double.NaN;
    }

    public String toString() {
      return String.valueOf(Double.NaN);
    }
  }
}
