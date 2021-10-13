package com.ak.rsm;

import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

interface Inverseable<M extends Measurement> {
  @Nonnull
  MediumLayers inverse(@Nonnull Collection<? extends M> measurements);

  @Nonnull
  RelativeMediumLayers inverseRelative(@Nonnull Collection<? extends M> measurements);

  @Nonnull
  @ParametersAreNonnullByDefault
  RelativeMediumLayers errors(Collection<TetrapolarSystem> systems, RelativeMediumLayers layers);
}
