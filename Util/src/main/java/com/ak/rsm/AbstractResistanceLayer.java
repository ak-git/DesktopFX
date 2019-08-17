package com.ak.rsm;

import java.util.Arrays;
import java.util.function.DoubleFunction;
import java.util.function.ToDoubleFunction;

import javax.annotation.Nonnull;

import com.ak.inverse.Inequality;
import com.ak.util.Strings;
import tec.uom.se.unit.Units;

abstract class AbstractResistanceLayer<U extends AbstractPotentialLayer> implements ToDoubleFunction<ToDoubleFunction<U>> {
  private final U uMns;
  private final U uPls;

  AbstractResistanceLayer(@Nonnull TetrapolarSystem electrodeSystem, DoubleFunction<U> potential) {
    uMns = potential.apply(electrodeSystem.radiusMns());
    uPls = potential.apply(electrodeSystem.radiusPls());
  }

  @Override
  public final double applyAsDouble(ToDoubleFunction<U> potentialValue) {
    return 2.0 * (potentialValue.applyAsDouble(uMns) - potentialValue.applyAsDouble(uPls));
  }

  abstract static class AbstractMedium {
    abstract String toString(@Nonnull TetrapolarSystem[] systems, @Nonnull double[] rOhms);

    final String toString(@Nonnull TetrapolarSystem[] systems, @Nonnull double[] rOhms, @Nonnull ToDoubleFunction<? super TetrapolarSystem> toDoubleFunction) {
      double[] predicted = Arrays.stream(systems).mapToDouble(toDoubleFunction).toArray();
      return String.format("%s; measured = %s, predicted = %s; L%s = %.6f", toString(),
          Strings.toString("%.3f", rOhms, Units.OHM),
          Strings.toString("%.3f", predicted, Units.OHM),
          Strings.low(2),
          Inequality.proportional().applyAsDouble(rOhms, predicted) / rOhms.length
      );
    }
  }
}
