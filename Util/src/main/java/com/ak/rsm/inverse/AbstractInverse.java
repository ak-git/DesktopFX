package com.ak.rsm.inverse;

import java.util.Collection;
import java.util.Collections;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.rsm.measurement.Measurements;
import com.ak.rsm.system.TetrapolarSystem;

abstract class AbstractInverse {
  @Nonnull
  private final Collection<TetrapolarSystem> systems;
  @Nonnegative
  private final double baseL;

  AbstractInverse(@Nonnull Collection<TetrapolarSystem> systems) {
    this.systems = Collections.unmodifiableCollection(systems);
    baseL = Measurements.getBaseL(systems);
  }

  @Nonnegative
  final double baseL() {
    return baseL;
  }

  @Nonnull
  final Collection<TetrapolarSystem> systems() {
    return systems;
  }
}