package com.ak.rsm.resistance;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.rsm.system.TetrapolarSystem;

public interface Resistance extends Resistivity {
  @Nonnull
  TetrapolarSystem system();

  @Nonnegative
  double ohms();
}
