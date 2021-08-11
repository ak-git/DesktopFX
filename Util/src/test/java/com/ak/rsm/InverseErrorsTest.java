package com.ak.rsm;

import java.util.Arrays;
import java.util.logging.Logger;
import java.util.stream.DoubleStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.math.Simplex;
import com.ak.util.CSVLineFileBuilder;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class InverseErrorsTest {
  private static final Logger LOGGER = Logger.getLogger(InverseErrorsTest.class.getName());

  @DataProvider(name = "inverseable")
  public static Object[][] inverseable() {
    return new Object[][] {
        {InverseStatic.INSTANCE},
        {InverseDynamic.INSTANCE},
    };
  }

  @Test(dataProvider = "inverseable", enabled = false)
  public void testCSV(@Nonnull Inverseable<? extends Measurement> inverseable) {
    double absErrorMilli = 0.001;
    CSVLineFileBuilder
        .of((k, hToL) -> inverseable.errors(
            Arrays.asList(TetrapolarSystem.systems2(absErrorMilli, 10.0)),
            new Layer2RelativeMedium(k, hToL)
        ))
        .xRange(-1.0, 1.0, 0.2)
        .yLog10Range(0.01, 1.0)
        .saveTo("k %s".formatted(inverseable.getClass().getSimpleName()),
            layers -> "%.4f".formatted(Math.abs(layers.k12AbsError() / layers.k12()) / (absErrorMilli / 50.0)))
        .saveTo("dk %s".formatted(inverseable.getClass().getSimpleName()),
            layers -> "%.4f".formatted(layers.k12AbsError() / (absErrorMilli / 50.0)))
        .saveTo("hToL %s".formatted(inverseable.getClass().getSimpleName()),
            layers -> "%.4f".formatted(Math.abs(layers.hToLAbsError() * 30 / 50.0) / (absErrorMilli / 50.0)))
        .generate();
  }

  @Test(enabled = false)
  public void test() {
    PointValuePair opt = Simplex.optimizeAll(InverseErrorsTest::single,
        new SimpleBounds(new double[] {0.0, 0.0}, new double[] {3.0, 3.0}),
        new double[] {0.1, 0.1}
    );
    LOGGER.info(Arrays.toString(opt.getPoint()));
  }

  @DataProvider(name = "single")
  public static Object[][] single() {
    return new Object[][] {
        {1.782422328929079, 0.280802508104057},
        {1.0 / 3.0, 5.0 / 3.0},
        {0.4142135623730951, 1.0 / 0.4142135623730951},
    };
  }

  @Test(dataProvider = "single", enabled = false)
  public void testSingle(@Nonnegative double sToL1, @Nonnegative double sToL2) {
    LOGGER.info("%.1f".formatted(single(new double[] {sToL1, sToL2})));
  }

  private static double single(@Nonnull double[] p) {
    return DoubleStream.iterate(0.01, h -> h < 1.0, h -> h += 0.01)
        .map(h -> single(p, h)).parallel().average().orElseThrow();
  }

  private static double single(@Nonnull double[] p, @Nonnegative double h) {
    double k = 1.0;

    double s1L = Math.min(p[0], p[1]);
    double s2L = Math.max(p[0], p[1]);
    double maxD = Math.max(1.0, s2L);
    double s2 = s2L / maxD;
    double L = s2 / s2L;
    double s1 = s1L * L;
    double absError = 0.001;

    RelativeMediumLayers errors = InverseDynamic.INSTANCE.errors(
        Arrays.asList(
            TetrapolarSystem.si(absError).s(s1).l(L),
            TetrapolarSystem.si(absError).s(s2).l(L)
        ),
        new Layer2RelativeMedium(k, h / L)
    );
    return errors.hToLAbsError() * L / absError;
  }
}
