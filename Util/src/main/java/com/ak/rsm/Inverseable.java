package com.ak.rsm;

import java.util.List;

import javax.annotation.Nonnull;

interface Inverseable<M extends Measurement> {
  @Nonnull
  MediumLayers inverse(@Nonnull List<? extends M> measurements);

  @Nonnull
  RelativeMediumLayers inverseRelative(@Nonnull List<? extends M> measurements);
}
