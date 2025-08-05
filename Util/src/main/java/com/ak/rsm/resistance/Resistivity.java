package com.ak.rsm.resistance;

import com.ak.rsm.system.TetrapolarSystem;

import java.util.Collection;

public interface Resistivity {
  /**
   * Gets <b>apparent</b> specific ohms which is corresponding to 1-layer model.
   *
   * @return <b>apparent</b> specific ohms in Ohm-m.
   */
  double resistivity();

  TetrapolarSystem system();

  static double getBaseL(Collection<? extends Resistivity> measurements) {
    return TetrapolarSystem.getBaseL(measurements.stream().map(Resistivity::system).toList());
  }
}
