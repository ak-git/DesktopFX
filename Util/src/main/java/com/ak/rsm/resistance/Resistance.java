package com.ak.rsm.resistance;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.rsm.system.TetrapolarSystem;

public interface Resistance {
  @Nonnull
  TetrapolarSystem system();

  @Nonnegative
  double ohms();

  @Nonnegative
  double resistivity();
}
