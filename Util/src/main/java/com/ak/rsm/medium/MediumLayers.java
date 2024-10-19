package com.ak.rsm.medium;

import com.ak.math.ValuePair;

public sealed interface MediumLayers permits AbstractMediumLayers {
  ValuePair rho();

  double[] getRMS();
}
