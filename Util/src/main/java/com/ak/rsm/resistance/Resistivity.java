package com.ak.rsm.resistance;

import com.ak.rsm.system.TetrapolarSystem;

import javax.annotation.Nonnegative;
import java.util.Collection;

public sealed interface Resistivity permits DerivativeResistivity, Resistance {
  /**
   * Gets <b>apparent</b> specific ohms which is corresponding to 1-layer model.
   *
   * @return <b>apparent</b> specific ohms in Ohm-m.
   */
  @Nonnegative
  double resistivity();

  TetrapolarSystem system();

  @Nonnegative
  static double getBaseL(Collection<? extends Resistivity> measurements) {
    return TetrapolarSystem.getBaseL(measurements.stream().map(Resistivity::system).toList());
  }
}
