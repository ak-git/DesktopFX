package com.ak.rsm.inverse;

import java.util.Collection;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.math.Simplex;
import com.ak.math.ValuePair;
import com.ak.rsm.measurement.DerivativeMeasurement;
import com.ak.rsm.measurement.Measurements;
import com.ak.rsm.measurement.TetrapolarDerivativeMeasurement;
import com.ak.rsm.medium.Layer2Medium;
import com.ak.rsm.medium.MediumLayers;
import com.ak.rsm.relative.Layer2RelativeMedium;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.util.CombinatoricsUtils;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class Inverse2Test {
  @DataProvider(name = "E-7694-system2")
  public static Object[][] e7694system2() {
    return new Object[][] {
        {
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system2(7.0)
                    .ofOhms(122.3, 199.0, 122.3 + 0.1, 199.0 + 0.4),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system2(7.0)
                    .ofOhms(122.3, 199.0, 122.3 + 0.3, 199.0 + 0.75),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 4).system2(7.0)
                    .ofOhms(122.3, 199.0, 122.3 + 0.6, 199.0 + 2.0),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 4).system2(7.0)
                    .ofOhms(122.3, 199.0, 122.3 - 0.3, 199.0 - 1.5)
            )
        },
    };
  }

  @DataProvider(name = "E-7694-105plus-system2")
  public static Object[][] e7694plus105system2() {
    return new Object[][] {
        {
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system2(7.0)
                    .ofOhms(120.8, 195.0, 120.8 + 0.15, 195.0 + 0.5),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system2(7.0)
                    .ofOhms(120.8, 195.0, 120.8 + 0.4, 195.0 + 1.0),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 4).system2(7.0)
                    .ofOhms(120.8, 195.0, 120.8 + 1.1, 195.0 + 2.5),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system2(7.0)
                    .ofOhms(120.8, 195.0, 120.8 - 0.15, 195.0 - 0.5),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 2).system2(7.0)
                    .ofOhms(120.8, 195.0, 120.8 - 0.3, 195.0 - 1.0),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 4).system2(7.0)
                    .ofOhms(120.8, 195.0, 120.8 - 0.6, 195.0 - 1.5)
            )
        },
    };
  }

  @DataProvider(name = "E-7694-system4")
  public static Object[][] e7694system4() {
    return new Object[][] {
        {
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(7.0)
                    .ofOhms(122.3, 199.0, 66.0 * 2, 202.0 * 2 - 66.0 * 2,
                        122.3 + 0.1, 199.0 + 0.4, (66.0 + 0.1) * 2, (202.0 + 0.25) * 2 - (66.0 + 0.1) * 2),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(7.0)
                    .ofOhms(122.3, 199.0, 66.0 * 2, 202.0 * 2 - 66.0 * 2,
                        122.3 + 0.3, 199.0 + 0.75, (66.0 + 0.2) * 2, (202.0 + 0.75) * 2 - (66.0 + 0.2) * 2),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 4).system4(7.0)
                    .ofOhms(122.3, 199.0, 66.0 * 2, 202.0 * 2 - 66.0 * 2,
                        122.3 + 0.6, 199.0 + 2.0, (66.0 + 0.6) * 2, (202.0 + 1.75) * 2 - (66.0 + 0.6) * 2),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 4).system4(7.0)
                    .ofOhms(122.3, 199.0, 66.0 * 2, 202.0 * 2 - 66.0 * 2,
                        122.3 - 0.3, 199.0 - 1.5, (66.0 - 0.3) * 2, (202.0 - 1.0) * 2 - (66.0 - 0.3) * 2)
            )
        },
    };
  }

  @DataProvider(name = "E-7694-105plus-system4")
  public static Object[][] e7694plus105system4() {
    return new Object[][] {
        {
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(7.0)
                    .ofOhms(120.8, 195.0, 66.9 * 2, 198.5 * 2 - 66.9 * 2,
                        120.8 + 0.15, 195.0 + 0.5, (66.9 + 0.15) * 2, (198.5 + 0.5) * 2 - (66.9 + 0.15) * 2),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(7.0)
                    .ofOhms(120.8, 195.0, 66.9 * 2, 198.5 * 2 - 66.9 * 2,
                        120.8 + 0.4, 195.0 + 1.0, (66.9 + 0.3) * 2, (198.5 + 1.25) * 2 - (66.9 + 0.3) * 2),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 4).system4(7.0)
                    .ofOhms(120.8, 195.0, 66.9 * 2, 198.5 * 2 - 66.9 * 2,
                        120.8 + 1.1, 195.0 + 2.5, (66.9 + 0.9) * 2, (198.5 + 2.5) * 2 - (66.9 + 0.9) * 2),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system4(7.0)
                    .ofOhms(120.8, 195.0, 66.9 * 2, 198.5 * 2 - 66.9 * 2,
                        120.8 - 0.15, 195.0 - 0.5, (66.9 - 0.125) * 2, (198.5 - 0.25) * 2 - (66.9 - 0.125) * 2),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 2).system4(7.0)
                    .ofOhms(120.8, 195.0, 66.9 * 2, 198.5 * 2 - 66.9 * 2,
                        120.8 - 0.3, 195.0 - 1.0, (66.9 - 0.25) * 2, (198.5 - 0.7) * 2 - (66.9 - 0.25) * 2),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 4).system4(7.0)
                    .ofOhms(120.8, 195.0, 66.9 * 2, 198.5 * 2 - 66.9 * 2,
                        120.8 - 0.6, 195.0 - 1.5, (66.9 - 0.5) * 2, (198.5 - 1.5) * 2 - (66.9 - 0.5) * 2)
            )
        },
    };
  }

  @DataProvider(name = "E-7694-2")
  public static Object[][] e7694_2() {
    return new Object[][] {
        {
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(7.0)
                    .ofOhms(123.5, 198.0, 68.0 * 2, 202.0 * 2 - 68.0 * 2,
                        123.5 + 0.5, 198.0 + 0.75, (68.0 + 0.25) * 2, (202.0 + 1.0) * 2 - (68.0 + 0.25) * 2),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(7.0)
                    .ofOhms(123.5, 198.0, 68.0 * 2, 202.0 * 2 - 68.0 * 2,
                        123.5 + 1.0, 198.0 + 2.0, (68.0 + 0.5) * 2, (202.0 + 2.0) * 2 - (68.0 + 0.5) * 2),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 4).system4(7.0)
                    .ofOhms(123.5, 198.0, 68.0 * 2, 202.0 * 2 - 68.0 * 2,
                        123.5 + 2.0, 198.0 + 5.0, (68.0 + 1.5) * 2, (202.0 + 6.0) * 2 - (68.0 + 1.5) * 2),

                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system4(7.0)
                    .ofOhms(123.5, 198.0, 68.0 * 2, 202.0 * 2 - 68.0 * 2,
                        123.5 - 0.3, 198.0 - 0.75, (68.0 - 0.2) * 2, (202.0 - 1.0) * 2 - (68.0 - 0.2) * 2),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 2).system4(7.0)
                    .ofOhms(123.5, 198.0, 68.0 * 2, 202.0 * 2 - 68.0 * 2,
                        123.5 - 0.7, 198.0 - 1.5, (68.0 - 0.375) * 2, (202.0 - 2.0) * 2 - (68.0 - 0.375) * 2),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 4).system4(7.0)
                    .ofOhms(123.5, 198.0, 68.0 * 2, 202.0 * 2 - 68.0 * 2,
                        123.5 - 1.25, 198.0 - 3.0, (68.0 - 0.75) * 2, (202.0 - 3.5) * 2 - (68.0 - 0.75) * 2)
            )
        },
    };
  }


  @Test(dataProvider = "E-7694-2", enabled = false)
  @ParametersAreNonnullByDefault
  public void testNoChanged(@Nonnull Collection<Collection<DerivativeMeasurement>> ms) {
    List<ToDoubleFunction<double[]>> dynamicInverses = ms.stream().map(DynamicInverse::of).toList();

    DoubleSummaryStatistics statisticsL = ms.stream().mapToDouble(Measurements::getBaseL).summaryStatistics();
    if (Double.compare(statisticsL.getMax(), statisticsL.getMin()) != 0) {
      throw new IllegalStateException("L is not equal for all electrode systems %s".formatted(statisticsL));
    }

    PointValuePair kwOptimal = Simplex.optimizeAll(
        kw -> dynamicInverses.stream().mapToDouble(value -> value.applyAsDouble(kw))
            .reduce(StrictMath::hypot).orElseThrow(),
        new Simplex.Bounds(-1.0, 1.0), new Simplex.Bounds(0.0, 1.0)
    );
    List<Layer2Medium> mediumList = ms.stream().map(dm -> new Layer2Medium(dm, new Layer2RelativeMedium(kwOptimal.getPoint()))).toList();
    var rho1 = mediumList.stream().map(MediumLayers::rho1).reduce(ValuePair::mergeWith).orElseThrow();
    var rho2 = mediumList.stream().map(MediumLayers::rho2).reduce(ValuePair::mergeWith).orElseThrow();
    var h = mediumList.stream().map(MediumLayers::h1).reduce(ValuePair::mergeWith).orElseThrow();
    Logger.getAnonymousLogger().info(() -> "%.6f; %s; %s; %s".formatted(kwOptimal.getValue(), rho1, rho2, h));
  }

  @Test(dataProvider = "E-7694-2", enabled = false)
  public void test(@Nonnull List<Collection<DerivativeMeasurement>> ms) {
    IntStream.range(1, ms.size())
        .mapToObj(value ->
            StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(CombinatoricsUtils.combinationsIterator(ms.size(), value), Spliterator.ORDERED),
                false)
        )
        .flatMap(Function.identity())
        .map(ints -> IntStream.of(ints).mapToObj(ms::get).collect(Collectors.toList()))
        .forEach(this::testNoChanged);
  }
}
