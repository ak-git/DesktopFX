package com.ak.rsm.inverse;

import java.util.Collection;
import java.util.Collections;
import java.util.function.UnaryOperator;

import javax.annotation.Nonnull;

import com.ak.rsm.relative.RelativeMediumLayers;
import com.ak.rsm.system.InexactTetrapolarSystem;

abstract class AbstractErrors extends AbstractInverse implements UnaryOperator<RelativeMediumLayers> {
  @Nonnull
  private final Collection<InexactTetrapolarSystem> inexactSystems;

  AbstractErrors(@Nonnull Collection<InexactTetrapolarSystem> inexactSystems) {
    super(inexactSystems.stream().map(InexactTetrapolarSystem::system).toList());
    this.inexactSystems = Collections.unmodifiableCollection(inexactSystems);
  }

  @Nonnull
  final Collection<InexactTetrapolarSystem> inexactSystems() {
    return inexactSystems;
  }
}
