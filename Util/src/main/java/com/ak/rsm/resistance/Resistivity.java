package com.ak.rsm.resistance;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

public interface Resistivity<T> {
  /**
   * Gets <b>apparent</b> specific ohms which is corresponding to 1-layer model.
   *
   * @return <b>apparent</b> specific ohms in Ohm-m.
   */
  @Nonnegative
  double resistivity();

  @Nonnull
  T system();
}
