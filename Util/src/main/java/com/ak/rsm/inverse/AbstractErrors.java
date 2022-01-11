package com.ak.rsm.inverse;

import java.util.Collection;
import java.util.Collections;
import java.util.function.UnaryOperator;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.rsm.measurement.Measurements;
import com.ak.rsm.relative.RelativeMediumLayers;
import com.ak.rsm.system.InexactTetrapolarSystem;
import com.ak.rsm.system.TetrapolarSystem;

abstract class AbstractErrors implements UnaryOperator<RelativeMediumLayers> {
  @Nonnull
  private final Collection<InexactTetrapolarSystem> inexactSystems;
  @Nonnull
  private final Collection<TetrapolarSystem> systems;
  @Nonnegative
  private final double baseL;

  AbstractErrors(@Nonnull Collection<InexactTetrapolarSystem> inexactSystems) {
    this.inexactSystems = Collections.unmodifiableCollection(inexactSystems);
    systems = Collections.unmodifiableCollection(inexactSystems.stream().map(InexactTetrapolarSystem::system).toList());
    baseL = Measurements.getBaseL(systems);
  }

  @Nonnegative
  final double baseL() {
    return baseL;
  }

  @Nonnull
  final Collection<InexactTetrapolarSystem> inexactSystems() {
    return inexactSystems;
  }

  @Nonnull
  final Collection<TetrapolarSystem> systems() {
    return systems;
  }
}
