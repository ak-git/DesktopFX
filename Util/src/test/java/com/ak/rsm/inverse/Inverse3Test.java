package com.ak.rsm.inverse;

import java.util.Arrays;
import java.util.Collection;
import java.util.DoubleSummaryStatistics;
import java.util.Iterator;
import java.util.List;
import java.util.function.ToDoubleFunction;
import java.util.logging.Logger;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.math.Simplex;
import com.ak.math.ValuePair;
import com.ak.rsm.apparent.Apparent3Rho;
import com.ak.rsm.measurement.DerivativeMeasurement;
import com.ak.rsm.measurement.Measurements;
import com.ak.rsm.measurement.TetrapolarDerivativeMeasurement;
import com.ak.rsm.system.Layers;
import com.ak.rsm.system.TetrapolarSystem;
import com.ak.util.Metrics;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class Inverse3Test {
  @DataProvider(name = "single")
  public static Object[][] single() {
    return new Object[][] {
        {
            TetrapolarDerivativeMeasurement.milli(0.1).dh(0.001).system4(10.0)
                .rho1(9.0).rho2(1.0).rho3(9.0).hStep(1.0).p(5, 5),
        },
    };
  }

  @Test(dataProvider = "single", invocationCount = 5, enabled = false)
  public void testSingle(@Nonnull Collection<? extends DerivativeMeasurement> ms) {
    double hStep = Metrics.fromMilli(1.0);
    ToDoubleFunction<double[]> dynamicInverse = DynamicInverse.of(ms, hStep);
    PointValuePair kwOptimal = Simplex.optimize(dynamicInverse::applyAsDouble,
        new SimpleBounds(new double[] {-1.0, -1.0, 1, 1}, new double[] {1.0, 1.0, 10, 10})
    );

    double[] point = kwOptimal.getPoint();
    var rho1 = getRho1(ms, point, hStep);
    var rho2 = ValuePair.Name.RHO_2.of(rho1.value() / Layers.getRho1ToRho2(point[0]), 0.0);
    var rho3 = ValuePair.Name.RHO_3.of(rho2.value() / Layers.getRho1ToRho2(point[1]), 0.0);
    Logger.getAnonymousLogger().info(() -> "%.6f %s; %s; %s; %s"
        .formatted(kwOptimal.getValue(), Arrays.toString(point), rho1, rho2, rho3));
  }

  @DataProvider(name = "noChanged")
  public static Object[][] noChanged() {
    int[] indentations = {0, -5, -10};
    return new Object[][] {
        {
            Arrays.stream(indentations).mapToObj(i ->
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.0001).system2(6.0)
                    .rho1(9.0).rho2(1.0).rho3(9.0).hStep(0.1).p(10, 50 + i)
            ).toList(),
            indentations
        },
    };
  }

  @Test(dataProvider = "noChanged", invocationCount = 5, enabled = false)
  @ParametersAreNonnullByDefault
  public void testNoChanged(Collection<Collection<? extends DerivativeMeasurement>> ms, int[] indentations) {
    double hStep = Metrics.fromMilli(0.1);
    List<ToDoubleFunction<double[]>> dynamicInverses = ms.stream().map(dm -> DynamicInverse.of(dm, hStep)).toList();

    DoubleSummaryStatistics statisticsL = ms.stream().mapToDouble(Measurements::getBaseL).summaryStatistics();
    if (Double.compare(statisticsL.getMax(), statisticsL.getMin()) != 0) {
      throw new IllegalStateException("L is not equal for all electrode systems %s".formatted(statisticsL));
    }

    PointValuePair kwOptimal = Simplex.optimize(
        kw -> {
          Iterator<double[]> iterator = Arrays.stream(indentations).mapToObj(x -> {
            double[] kwIndent = kw.clone();
            kwIndent[3] += x;
            return kwIndent;
          }).iterator();

          return dynamicInverses.stream().mapToDouble(value -> value.applyAsDouble(iterator.next()))
              .reduce(StrictMath::hypot).orElseThrow();
        },
        new SimpleBounds(new double[] {-1.0, -1.0, 1, 1 + Arrays.stream(indentations).map(Math::abs).max().orElse(0)},
            new double[] {1.0, 1.0, 100, 100})
    );

    var rho1 = ms.stream().map(dm -> getRho1(dm, kwOptimal.getPoint(), hStep)).reduce(ValuePair::mergeWith).orElseThrow();
    var rho2 = ValuePair.Name.RHO_2.of(rho1.value() / Layers.getRho1ToRho2(kwOptimal.getPoint()[0]), 0.0);
    var rho3 = ValuePair.Name.RHO_3.of(rho2.value() / Layers.getRho1ToRho2(kwOptimal.getPoint()[1]), 0.0);
    Logger.getAnonymousLogger().info(() -> "%.6f %s; %s; %s; %s"
        .formatted(kwOptimal.getValue(), Arrays.toString(kwOptimal.getPoint()), rho1, rho2, rho3));
  }

  @Nonnegative
  @ParametersAreNonnullByDefault
  private static ValuePair getRho1(Collection<? extends DerivativeMeasurement> measurements, double[] kw, @Nonnegative double hStep) {
    return measurements.stream().parallel()
        .map(measurement -> {
          TetrapolarSystem s = measurement.system();
          double normApparent = Apparent3Rho.newNormalizedApparent2Rho(s.relativeSystem())
              .value(
                  kw[0], kw[1],
                  hStep / s.lCC(),
                  (int) Math.round(kw[2]), (int) Math.round(kw[3])
              );
          return ValuePair.Name.RHO_1.of(measurement.resistivity() / normApparent,
              0.0
          );
        })
        .reduce(ValuePair::mergeWith).orElseThrow();
  }
}
