package com.ak.rsm.inverse;

import java.util.Arrays;
import java.util.Collection;
import java.util.DoubleSummaryStatistics;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.math.Simplex;
import com.ak.rsm.measurement.DerivativeMeasurement;
import com.ak.rsm.measurement.Measurements;
import com.ak.rsm.measurement.TetrapolarDerivativeMeasurement;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class Inverse3Test {
  @DataProvider(name = "single")
  public static Object[][] single() {
    return new Object[][] {
        {
            TetrapolarDerivativeMeasurement.milli(0.1).dh(0.01).system4(10.0)
                .rho1(9.0).rho2(1.0).rho3(9.0).hStep(1.0).p(5, 5),
        },
    };
  }

  @Test(dataProvider = "single", invocationCount = 5, enabled = false)
  public void testSingle(@Nonnull Collection<? extends DerivativeMeasurement> ms) {
    DynamicInverse dynamicInverse = new DynamicInverse(ms);
    PointValuePair kwOptimal = Simplex.optimize(dynamicInverse::applyAsDouble,
        new SimpleBounds(new double[] {-1.0, -1.0, 1, 1}, new double[] {1.0, 1.0, 10, 10}),
        new double[] {0.01, 0.01, 1, 1}
    );
    Logger.getAnonymousLogger().info(() -> "%.6f %s".formatted(kwOptimal.getValue(), Arrays.toString(kwOptimal.getPoint())));
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

//                2021-10-22
        {
            List.of(
//                189.75, 0,
                TetrapolarDerivativeMeasurement.milli(0.1).dh(Double.NaN).system2(6.0)
                    .rho(
                        4.36561500845104, 4.49418992805016, -0.178420874720668, -0.183605798682716
                    ),
//                199.75, -2.1,
                TetrapolarDerivativeMeasurement.milli(0.1).dh(Double.NaN).system2(6.0)
                    .rho(
                        4.37968045714554, 4.51061464886706, -0.244006351213963, -0.29392479021624
                    ),
//                209.75, -4.2,
                TetrapolarDerivativeMeasurement.milli(0.1).dh(Double.NaN).system2(6.0)
                    .rho(
                        4.40723928934415, 4.54541460187173, -0.391702185525521, -0.383635767588383
                    ),
//                214.25, -5.46,
                TetrapolarDerivativeMeasurement.milli(0.1).dh(Double.NaN).system2(6.0)
                    .rho(
                        4.41859231744725, 4.55430142272032, -0.466863160010698, -0.409174798561001
                    )
            ),
            new int[] {0, -21, -42, -55}
        },
    };
  }

  @Test(dataProvider = "noChanged", invocationCount = 5, enabled = false)
  @ParametersAreNonnullByDefault
  public void testNoChanged(Collection<Collection<? extends DerivativeMeasurement>> ms, int[] indentations) {
    List<DynamicInverse> dynamicInverses = ms.stream().map(DynamicInverse::new).toList();

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
            new double[] {1.0, 1.0, 100, 100}),
        new double[] {0.01, 0.01, 10, 10}
    );
    Logger.getAnonymousLogger().info(() -> "%.6f %s".formatted(kwOptimal.getValue(), Arrays.toString(kwOptimal.getPoint())));
  }
}
