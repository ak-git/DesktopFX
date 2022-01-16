package com.ak.rsm.resistance;

import javax.annotation.Nonnegative;

import com.ak.rsm.system.TetrapolarSystem;

public interface Resistance extends Resistivity<TetrapolarSystem> {
  @Nonnegative
  double ohms();
}
