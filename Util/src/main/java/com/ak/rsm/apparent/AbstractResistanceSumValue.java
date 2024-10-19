package com.ak.rsm.apparent;

import com.ak.rsm.system.Layers;
import com.ak.rsm.system.RelativeTetrapolarSystem;

import javax.annotation.Nonnegative;
import java.util.function.DoubleBinaryOperator;
import java.util.function.IntToDoubleFunction;

import static java.lang.StrictMath.hypot;

abstract class AbstractResistanceSumValue extends AbstractApparent implements ResistanceSumValue {
  AbstractResistanceSumValue(RelativeTetrapolarSystem system) {
    super(system);
  }

  @Override
  public final double value(@Nonnegative double hToL, IntToDoubleFunction qn) {
    DoubleBinaryOperator sum = sum(hToL);
    return multiply(Layers.sum(n -> qn.applyAsDouble(n) * (sum.applyAsDouble(-1.0, n) - sum.applyAsDouble(1.0, n))));
  }

  DoubleBinaryOperator sum(double hToL) {
    return (sign, n) -> 1.0 / hypot(factor(sign), 4.0 * n * hToL);
  }

  abstract double multiply(double sums);
}
