package com.ak.rsm;

import javax.annotation.Nonnegative;

interface Prediction {
  @Nonnegative
  double getInequalityL2();
}
