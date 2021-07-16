package com.ak.rsm;

import java.util.Collection;

import javax.annotation.Nonnull;

interface Inverseable<M extends Measurement> {
  @Nonnull
  MediumLayers inverse(@Nonnull Collection<? extends M> measurements);

  @Nonnull
  RelativeMediumLayers inverseRelative(@Nonnull Collection<? extends M> measurements);
}
