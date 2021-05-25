package com.ak.rsm;

import javax.annotation.Nonnull;

interface RelativeMediumLayers<D> {
  RelativeMediumLayers<Double> SINGLE_LAYER = new RelativeMediumLayers<>() {
    @Override
    public Double k12() {
      return 0.0;
    }

    @Override
    public Double hToL() {
      return Double.NaN;
    }
  };
  RelativeMediumLayers<Double> NAN = new RelativeMediumLayers<>() {
    @Override
    public Double k12() {
      return Double.NaN;
    }

    @Override
    public Double hToL() {
      return Double.NaN;
    }
  };

  @Nonnull
  D k12();

  @Nonnull
  D hToL();
}
