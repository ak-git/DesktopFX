package com.ak.rsm.inverse;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.logging.Logger;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class InverseErrorsTest {
  private static final Logger LOGGER = Logger.getLogger(InverseErrorsTest.class.getName());

  static Stream<Arguments> inverseable() {
    return Stream.of(
        arguments(
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
        ),
        arguments(
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
        )
    );
  }

  @ParameterizedTest
  @MethodSource("inverseable")
  @Disabled("ignored com.ak.rsm.inverse.InverseErrorsTest.testCSV")
  void testCSV(@Nonnull Function<Collection<InexactTetrapolarSystem>, UnaryOperator<RelativeMediumLayers>> builder) {
    assertThatNoException().isThrownBy(() ->
        CSVLineFileBuilder
            .of((k, hToDim) -> single(new double[] {0.1, 1.9}, k, hToDim, builder))
            .xRange(-1.0, 1.0, 0.2)
            .yLogRange(0.001, 1.0)
            .saveTo("kRise %s".formatted(builder.toString()),
                layers -> "%.4f".formatted(layers[0]))
            .saveTo("hRise %s".formatted(builder.toString()),
                layers -> "%.4f".formatted(layers[1]))
            .generate()
    );
  }

  @ParameterizedTest
  @MethodSource("inverseable")
  @Disabled("ignored com.ak.rsm.inverse.InverseErrorsTest.testCSV2")
  void testCSV2(@Nonnull Function<Collection<InexactTetrapolarSystem>, UnaryOperator<RelativeMediumLayers>> builder) {
    assertThatNoException().isThrownBy(() ->
        CSVLineFileBuilder
            .of((k, hToDim) -> single(new double[] {1.0 / 3.0, 5.0 / 3.0}, k, hToDim, builder))
            .xRange(-1.0, 1.0, 0.2)
            .yLogRange(0.001, 1.0)
            .saveTo("k2Rise %s".formatted(builder.toString()),
                layers -> "%.4f".formatted(layers[0]))
            .saveTo("h2Rise %s".formatted(builder.toString()),
                layers -> "%.4f".formatted(layers[1]))
            .generate()
    );
  }

  @ParameterizedTest
  @MethodSource("inverseable")
  @Disabled("ignored com.ak.rsm.inverse.InverseErrorsTest.testOptimalSL")
  void testOptimalSL(@Nonnull Function<Collection<InexactTetrapolarSystem>, UnaryOperator<RelativeMediumLayers>> builder) {
    PointValuePair opt = Simplex.optimizeAll(point -> single(point, builder),
        new Simplex.Bounds(0.1, 1.1), new Simplex.Bounds(0.9, 2.0)
    );
    assertNotNull(opt);
    LOGGER.info(Arrays.toString(opt.getPoint()));
  }

  @ParameterizedTest
  @MethodSource("inverseable")
  @Disabled("ignored com.ak.rsm.inverse.InverseErrorsTest.testCSVasSL")
  void testCSVasSL(@Nonnull Function<Collection<InexactTetrapolarSystem>, UnaryOperator<RelativeMediumLayers>> builder) {
    assertThatNoException().isThrownBy(() ->
        CSVLineFileBuilder
            .of((sToL1, sToL2) -> single(new double[] {sToL1, sToL2}, builder))
            .xRange(0.1, 0.9, 0.1)
            .yRange(1.1, 2.0, 0.1)
            .saveTo("k %s".formatted(builder.toString()), "%.4f"::formatted)
            .generate()
    );
  }

  static Stream<Arguments> single() {
    return Stream.of(
        arguments(
            1.0 / 3.0, 5.0 / 3.0, (Function<Collection<InexactTetrapolarSystem>, UnaryOperator<RelativeMediumLayers>>) DynamicErrors::new
        ),
        arguments(
            0.1, 1.9, (Function<Collection<InexactTetrapolarSystem>, UnaryOperator<RelativeMediumLayers>>) StaticErrors::new
        )
    );
  }

  @ParameterizedTest
  @MethodSource("single")
  @Disabled("ignored com.ak.rsm.inverse.InverseErrorsTest.testSingle")
  void testSingle(@Nonnegative double sToL1, @Nonnegative double sToL2,
                  @Nonnull Function<Collection<InexactTetrapolarSystem>, UnaryOperator<RelativeMediumLayers>> builder) {
    double[] single = single(new double[] {sToL1, sToL2}, 1.0, 25.0 / 50.0, builder);
    assertThat(single).isNotEmpty();
    LOGGER.info(Arrays.toString(single));
  }

  @ParameterizedTest
  @MethodSource("single")
  @Disabled("ignored com.ak.rsm.inverse.InverseErrorsTest.testSingleSum")
  void testSingleSum(@Nonnegative double sToL1, @Nonnegative double sToL2,
                     @Nonnull Function<Collection<InexactTetrapolarSystem>, UnaryOperator<RelativeMediumLayers>> builder) {
    double single = single(new double[] {sToL1, sToL2}, builder);
    assertThat(single).isPositive();
    LOGGER.info("%.3f".formatted(single));
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

