package com.ak.rsm.resistance;

import javax.annotation.Nonnegative;

public interface Resistance extends Resistivity {
  @Nonnegative
  double ohms();
}
