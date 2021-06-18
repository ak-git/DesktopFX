package com.ak.rsm;

import javax.annotation.Nonnegative;

interface RelativeMediumLayers {
  RelativeMediumLayers SINGLE_LAYER = new RelativeMediumLayers() {
    @Override
    public double k12() {
      return 0.0;
    }

    @Override
    public double hToL() {
      return Double.NaN;
    }
  };
  RelativeMediumLayers NAN = new RelativeMediumLayers() {
    @Override
    public double k12() {
      return Double.NaN;
    }

    @Override
    public double hToL() {
      return Double.NaN;
    }
  };

  double k12();

  @Nonnegative
  double hToL();

  @Nonnegative
  default double k12AbsError() {
    return 0.0;
  }

  @Nonnegative
  default double hToLAbsError() {
    return 0.0;
  }
}
