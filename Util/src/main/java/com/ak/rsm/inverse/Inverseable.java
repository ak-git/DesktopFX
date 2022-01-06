package com.ak.rsm.inverse;

import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.rsm.measurement.Measurement;
import com.ak.rsm.medium.MediumLayers;
import com.ak.rsm.relative.RelativeMediumLayers;
import com.ak.rsm.system.InexactTetrapolarSystem;

interface Inverseable<M extends Measurement> {
  @Nonnull
  MediumLayers inverse(@Nonnull Collection<? extends M> measurements);

  @Nonnull
  RelativeMediumLayers inverseRelative(@Nonnull Collection<? extends M> measurements);

  @Nonnull
  @ParametersAreNonnullByDefault
  RelativeMediumLayers errors(Collection<InexactTetrapolarSystem> systems, RelativeMediumLayers layers);
}

