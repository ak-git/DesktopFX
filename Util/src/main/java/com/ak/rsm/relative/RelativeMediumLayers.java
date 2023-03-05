package com.ak.rsm.relative;

import javax.annotation.Nonnegative;

public sealed interface RelativeMediumLayers permits Layer1RelativeMedium, Layer2RelativeMedium {
  double k12();

  @Nonnegative
  double hToL();

  @Nonnegative
  default double k12AbsError() {
    return 0.0;
  }

  @Nonnegative
  default double hToLAbsError() {
    return 0.0;
  }
}
