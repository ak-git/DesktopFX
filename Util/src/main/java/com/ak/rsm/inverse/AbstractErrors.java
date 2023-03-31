package com.ak.rsm.inverse;

import com.ak.rsm.system.InexactTetrapolarSystem;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;

abstract class AbstractErrors extends AbstractInverse {
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
