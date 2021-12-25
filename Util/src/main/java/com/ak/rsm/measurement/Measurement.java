package com.ak.rsm.measurement;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.rsm.system.InexactTetrapolarSystem;

public interface Measurement {
  @Nonnull
  InexactTetrapolarSystem system();

  @Nonnegative
  double resistivity();

  @Nonnull
  Measurement merge(@Nonnull Measurement that);
}
