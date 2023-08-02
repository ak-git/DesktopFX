package com.ak.rsm.inverse;

import com.ak.rsm.system.InexactTetrapolarSystem;
import com.ak.rsm.system.TetrapolarSystem;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;

abstract class AbstractErrors {
  @Nonnull
  private final Collection<InexactTetrapolarSystem> inexactSystems;
  @Nonnull
  private final Collection<TetrapolarSystem> systems;
  @Nonnegative
  private final double baseL;

  AbstractErrors(@Nonnull Collection<InexactTetrapolarSystem> inexactSystems) {
    this.inexactSystems = Collections.unmodifiableCollection(inexactSystems);
    systems = inexactSystems.stream().map(InexactTetrapolarSystem::system).toList();
    baseL = TetrapolarSystem.getBaseL(systems.stream());
  }

  @Nonnull
  final Collection<InexactTetrapolarSystem> inexactSystems() {
    return inexactSystems;
  }

  @Nonnull
  final Collection<TetrapolarSystem> systems() {
    return systems;
  }

  @Nonnegative
  final double baseL() {
    return baseL;
  }
}
