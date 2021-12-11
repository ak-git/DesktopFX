package com.ak.rsm;

import java.util.Arrays;
import java.util.logging.Logger;
import java.util.stream.DoubleStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

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
    CSVLineFileBuilder
        .of((k, hToDim) -> single(new double[] {0.1, 1.9}, k, hToDim, inverseable))
        .xRange(-1.0, 1.0, 0.2)
        .yLogRange(0.001, 1.0)
        .saveTo("kRise %s".formatted(inverseable.getClass().getSimpleName()),
            layers -> "%.4f".formatted(layers[0]))
        .saveTo("hRise %s".formatted(inverseable.getClass().getSimpleName()),
            layers -> "%.4f".formatted(layers[1]))
        .generate();
  }

  @Test(dataProvider = "inverseable", enabled = false)
  public void testCSV2(@Nonnull Inverseable<? extends Measurement> inverseable) {
    CSVLineFileBuilder
        .of((k, hToDim) -> single(new double[] {1.0 / 3.0, 5.0 / 3.0}, k, hToDim, inverseable))
        .xRange(-1.0, 1.0, 0.2)
        .yLogRange(0.001, 1.0)
        .saveTo("k2Rise %s".formatted(inverseable.getClass().getSimpleName()),
            layers -> "%.4f".formatted(layers[0]))
        .saveTo("h2Rise %s".formatted(inverseable.getClass().getSimpleName()),
            layers -> "%.4f".formatted(layers[1]))
        .generate();
  }

  @Test(dataProvider = "inverseable", enabled = false)
  public void testOptimalSL(@Nonnull Inverseable<? extends Measurement> inverseable) {
    PointValuePair opt = Simplex.optimizeAll(point -> single(point, inverseable),
        new SimpleBounds(new double[] {0.1, 0.9}, new double[] {1.1, 2.0}),
        new double[] {0.1, 0.1}
    );
    LOGGER.info(Arrays.toString(opt.getPoint()));
  }

  @Test(dataProvider = "inverseable", enabled = false)
  public void testCSVasSL(@Nonnull Inverseable<? extends Measurement> inverseable) {
    CSVLineFileBuilder
        .of((sToL1, sToL2) -> single(new double[] {sToL1, sToL2}, inverseable))
        .xRange(0.1, 0.9, 0.1)
        .yRange(1.1, 2.0, 0.1)
        .saveTo("k %s".formatted(inverseable.getClass().getSimpleName()), "%.4f"::formatted)
        .generate();
  }

  @DataProvider(name = "single")
  public static Object[][] single() {
    return new Object[][] {
        {1.0 / 3.0, 5.0 / 3.0},
        {0.1, 1.9},
    };
  }

  @Test(dataProvider = "single", enabled = false)
  public void testSingle(@Nonnegative double sToL1, @Nonnegative double sToL2) {
    LOGGER.info(Arrays.toString(single(new double[] {sToL1, sToL2}, 1.0, 25.0 / 50.0, InverseDynamic.INSTANCE)));
    LOGGER.info(Arrays.toString(single(new double[] {sToL1, sToL2}, 1.0, 25.0 / 50.0, InverseStatic.INSTANCE)));
  }

  @Test(dataProvider = "single", enabled = false)
  public void testSingleSum(@Nonnegative double sToL1, @Nonnegative double sToL2) {
    LOGGER.info("%.3f".formatted(single(new double[] {sToL1, sToL2}, InverseDynamic.INSTANCE)));
    LOGGER.info("%.3f".formatted(single(new double[] {sToL1, sToL2}, InverseStatic.INSTANCE)));
  }

  @ParametersAreNonnullByDefault
  private static double single(double[] p, Inverseable<? extends Measurement> inverse) {
    return DoubleStream.iterate(0.0001, h -> h < 1.0, h -> h += 0.0001)
        .map(h -> single(p, 1.0, h, inverse)[0]).parallel().sum();
  }

  @Nonnull
  @ParametersAreNonnullByDefault
  private static double[] single(double[] p, double k, @Nonnegative double hToDim, Inverseable<?> inverseable) {
    double s1L = Math.min(p[0], p[1]);
    double s2L = Math.max(p[0], p[1]);
    double maxD = Math.max(1.0, s2L);
    double s2 = s2L / maxD;
    double L = s2 / s2L;
    double s1 = s1L * L;
    double absError = 1.0e-4;

    RelativeMediumLayers errors = inverseable.errors(
        Arrays.asList(
            TetrapolarSystem.si(absError).s(s1).l(L),
            TetrapolarSystem.si(absError).s(s2).l(L)
        ),
        new Layer2RelativeMedium(k, hToDim / L)
    );

    double oneDim = Math.max(Math.max(s1, s2), L);
    return new double[] {
        Math.abs(errors.k12AbsError() / errors.k12()) / (absError / oneDim),
        Math.abs(errors.hToLAbsError() * L / oneDim) / (absError / oneDim),
    };
  }
}
