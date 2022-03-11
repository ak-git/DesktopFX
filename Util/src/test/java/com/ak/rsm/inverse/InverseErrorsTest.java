package com.ak.rsm.inverse;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.logging.Logger;
import java.util.stream.DoubleStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.math.Simplex;
import com.ak.rsm.relative.Layer2RelativeMedium;
import com.ak.rsm.relative.RelativeMediumLayers;
import com.ak.rsm.system.InexactTetrapolarSystem;
import com.ak.rsm.system.TetrapolarSystem;
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
        {
            new Function<Collection<InexactTetrapolarSystem>, UnaryOperator<RelativeMediumLayers>>() {
              @Override
              public UnaryOperator<RelativeMediumLayers> apply(Collection<InexactTetrapolarSystem> inexactSystems) {
                return new StaticErrors(inexactSystems);
              }

              @Override
              public String toString() {
                return "StaticErrors";
              }
            }
        },
        {
            new Function<Collection<InexactTetrapolarSystem>, UnaryOperator<RelativeMediumLayers>>() {
              @Override
              public UnaryOperator<RelativeMediumLayers> apply(Collection<InexactTetrapolarSystem> inexactSystems) {
                return new DynamicErrors(inexactSystems);
              }

              @Override
              public String toString() {
                return "DynamicErrors";
              }
            }
        }
    };
  }

  @Test(dataProvider = "inverseable", enabled = false)
  public void testCSV(@Nonnull Function<Collection<InexactTetrapolarSystem>, UnaryOperator<RelativeMediumLayers>> builder) {
    CSVLineFileBuilder
        .of((k, hToDim) -> single(new double[] {0.1, 1.9}, k, hToDim, builder))
        .xRange(-1.0, 1.0, 0.2)
        .yLogRange(0.001, 1.0)
        .saveTo("kRise %s".formatted(builder.toString()),
            layers -> "%.4f".formatted(layers[0]))
        .saveTo("hRise %s".formatted(builder.toString()),
            layers -> "%.4f".formatted(layers[1]))
        .generate();
  }

  @Test(dataProvider = "inverseable", enabled = false)
  public void testCSV2(@Nonnull Function<Collection<InexactTetrapolarSystem>, UnaryOperator<RelativeMediumLayers>> builder) {
    CSVLineFileBuilder
        .of((k, hToDim) -> single(new double[] {1.0 / 3.0, 5.0 / 3.0}, k, hToDim, builder))
        .xRange(-1.0, 1.0, 0.2)
        .yLogRange(0.001, 1.0)
        .saveTo("k2Rise %s".formatted(builder.toString()),
            layers -> "%.4f".formatted(layers[0]))
        .saveTo("h2Rise %s".formatted(builder.toString()),
            layers -> "%.4f".formatted(layers[1]))
        .generate();
  }

  @Test(dataProvider = "inverseable", enabled = false)
  public void testOptimalSL(@Nonnull Function<Collection<InexactTetrapolarSystem>, UnaryOperator<RelativeMediumLayers>> builder) {
    PointValuePair opt = Simplex.optimize(point -> single(point, builder),
        new SimpleBounds(new double[] {0.1, 0.9}, new double[] {1.1, 2.0})
    );
    LOGGER.info(Arrays.toString(opt.getPoint()));
  }

  @Test(dataProvider = "inverseable", enabled = false)
  public void testCSVasSL(@Nonnull Function<Collection<InexactTetrapolarSystem>, UnaryOperator<RelativeMediumLayers>> builder) {
    CSVLineFileBuilder
        .of((sToL1, sToL2) -> single(new double[] {sToL1, sToL2}, builder))
        .xRange(0.1, 0.9, 0.1)
        .yRange(1.1, 2.0, 0.1)
        .saveTo("k %s".formatted(builder.toString()), "%.4f"::formatted)
        .generate();
  }

  @DataProvider(name = "single")
  public static Object[][] single() {
    return new Object[][] {
        {1.0 / 3.0, 5.0 / 3.0, (Function<Collection<InexactTetrapolarSystem>, UnaryOperator<RelativeMediumLayers>>) DynamicErrors::new},
        {0.1, 1.9, (Function<Collection<InexactTetrapolarSystem>, UnaryOperator<RelativeMediumLayers>>) StaticErrors::new},
    };
  }

  @Test(dataProvider = "single", enabled = false)
  public void testSingle(@Nonnegative double sToL1, @Nonnegative double sToL2,
                         @Nonnull Function<Collection<InexactTetrapolarSystem>, UnaryOperator<RelativeMediumLayers>> builder) {
    LOGGER.info(Arrays.toString(single(new double[] {sToL1, sToL2}, 1.0, 25.0 / 50.0, builder)));
  }

  @Test(dataProvider = "single", enabled = false)
  public void testSingleSum(@Nonnegative double sToL1, @Nonnegative double sToL2,
                            @Nonnull Function<Collection<InexactTetrapolarSystem>, UnaryOperator<RelativeMediumLayers>> builder) {
    LOGGER.info("%.3f".formatted(single(new double[] {sToL1, sToL2}, builder)));
  }

  @ParametersAreNonnullByDefault
  private static double single(double[] p, Function<Collection<InexactTetrapolarSystem>, UnaryOperator<RelativeMediumLayers>> builder) {
    double step = 0.001;
    return DoubleStream.iterate(step, h -> h < 1.0, h -> h += step)
        .map(h -> single(p, 1.0, h, builder)[0]).parallel().sum();
  }

  @Nonnull
  @ParametersAreNonnullByDefault
  private static double[] single(double[] p, double k, @Nonnegative double hToDim,
                                 Function<Collection<InexactTetrapolarSystem>, UnaryOperator<RelativeMediumLayers>> builder) {
    double s1L = Math.min(p[0], p[1]);
    double s2L = Math.max(p[0], p[1]);
    double maxD = Math.max(1.0, s2L);
    double s2 = s2L / maxD;
    double L = s2 / s2L;
    double s1 = s1L * L;
    double absError = 1.0e-4;

    RelativeMediumLayers errors = builder
        .apply(
            List.of(
                new InexactTetrapolarSystem(absError, new TetrapolarSystem(s1, L)),
                new InexactTetrapolarSystem(absError, new TetrapolarSystem(s2, L))
            )
        )
        .apply(new Layer2RelativeMedium(k, hToDim / L));

    double oneDim = Math.max(Math.max(s1, s2), L);
    return new double[] {
        Math.abs(errors.k12AbsError() / errors.k12()) / (absError / oneDim),
        Math.abs(errors.hToLAbsError() * L / oneDim) / (absError / oneDim),
    };
  }
}

