package com.ak.rsm2;

import com.ak.math.Simplex;
import com.ak.math.ValuePair;
import com.ak.util.Strings;
import org.apache.commons.math4.legacy.optim.InitialGuess;
import org.apache.commons.math4.legacy.optim.MaxEval;
import org.apache.commons.math4.legacy.optim.PointValuePair;
import org.apache.commons.math4.legacy.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math4.legacy.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math4.legacy.optim.nonlinear.scalar.noderiv.NelderMeadTransform;
import org.apache.commons.math4.legacy.optim.nonlinear.scalar.noderiv.SimplexOptimizer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.function.DoubleFunction;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.Stream;

class InverseTest {
  private static final Logger LOGGER = LoggerFactory.getLogger(InverseTest.class);

  @Nested
  class WaterTest {
    @BeforeAll
    static void dataErrorNorm() {
      double dataErrorNorm = Stream.of(
              ElectrodeSystem.ofMilli().tetrapolar(10.0, 30.0).absError(0.1).build(),
              ElectrodeSystem.ofMilli().tetrapolar(50.0, 30.0).absError(0.1).build())
          .mapToDouble(ElectrodeSystem.Inexact::dataErrorNorm).reduce(Math::hypot).orElseThrow();
      LOGGER.atInfo().addKeyValue("data Error Norm", "%.4f".formatted(dataErrorNorm)).log(Strings.EMPTY);
    }

    @Disabled("26.734, 46.074, 26.719, 46.028, 0.450")
    @ParameterizedTest
    @CsvSource(delimiter = ',', textBlock = """
        30.971, 61.860, 31.278, 62.479, -0.05
        16.761, 32.246, 16.821, 32.383, -0.05
        13.338, 23.903, 13.357, 23.953, -0.05
        12.187, 20.567, 12.194, 20.589, -0.05
        11.710, 18.986, 11.714, 18.998, -0.05
        11.482, 18.152, 11.484, 18.158, -0.05
        11.361, 17.674, 11.362, 17.678, -0.05
        """)
    void waterParameters(double r1, double r2, double r1After, double r2After, double dHmm) {
      Collection<Misfit> misfits = List.of(
          Misfit.builder()
              .ofMilli(s -> s.tetrapolar(10.0, 30.0).absError(0.1))
              .measurements(m -> m.ohms(r1).dhMilli(dHmm).thenOhms(r1After))
              .build(),
          Misfit.builder()
              .ofMilli(s -> s.tetrapolar(50.0, 30.0).absError(0.1))
              .measurements(m -> m.ohms(r2).dhMilli(dHmm).thenOhms(r2After))
              .build()
      );

      DoubleFunction<Model.Layer2Relative> find = alpha -> {
        PointValuePair optimized = Simplex.optimizeAll(point -> {
              Model.Layer2Relative m = new Model.Layer2Relative(point[0], point[1]);
              double s = misfits.stream().mapToDouble(f -> f.regularization(Misfit.Regularization.ZERO_MAX_LOG).applyAsDouble(m)).sum();
              if (Double.isFinite(s)) {
                double misfit = misfits.stream().mapToDouble(f -> f.misfit().applyAsDouble(m)).reduce(Math::hypot).orElseThrow();
                return misfit * misfit + Math.abs(alpha) * s;
              }
              else {
                return Double.POSITIVE_INFINITY;
              }
            },
            new Simplex.Bounds(-1.0, 1.0), new Simplex.Bounds(0.0, 0.04));
        double[] point = optimized.getPoint();
        return new Model.Layer2Relative(point[0], point[1]);
      };

      DoubleUnaryOperator withAlpha = new DoubleUnaryOperator() {
        private final double dataErrorNorm = misfits.stream().mapToDouble(Misfit::dataErrorNorm).reduce(Math::hypot).orElseThrow();

        @Override
        public double applyAsDouble(double alpha) {
          if (alpha < 0) {
            return Double.POSITIVE_INFINITY;
          }
          else {
            Model.Layer2Relative m = find.apply(alpha);
            double misfit = misfits.stream().mapToDouble(f -> f.misfit().applyAsDouble(m)).reduce(Math::hypot).orElseThrow();
            LOGGER.atInfo().addKeyValue("alpha", "%.4f".formatted(alpha)).addKeyValue("misfit", "%.4f".formatted(misfit))
                .log(() -> "%s; %s".formatted(
                    ValuePair.Name.K12.of(find.apply(alpha).k().value(), 0.0),
                    ValuePair.Name.H.of(find.apply(alpha).h(), 0.0))
                );
            double v = misfit - dataErrorNorm;
            return v * v;
          }
        }
      };

      PointValuePair optimized = new SimplexOptimizer(0.000_000_1, 0.000_01)
          .optimize(new MaxEval(100), new ObjectiveFunction(point -> withAlpha.applyAsDouble(point[0])),
              GoalType.MINIMIZE, org.apache.commons.math4.legacy.optim.nonlinear.scalar.noderiv.Simplex.alongAxes(new double[] {0.001}),
              new NelderMeadTransform(), new InitialGuess(new double[] {0.0})
          );
      double alpha = optimized.getPoint()[0];
      LOGGER.atWarn().addKeyValue("alpha", () -> "%.4f".formatted(alpha))
          .log("%s; %s".formatted(
              ValuePair.Name.K12.of(find.apply(alpha).k().value(), 0.0),
              ValuePair.Name.H.of(find.apply(alpha).h(), 0.0)));
    }
  }
}
