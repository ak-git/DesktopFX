package com.ak.rsm.inverse;

import com.ak.math.Simplex;
import com.ak.rsm.measurement.DerivativeMeasurement;
import com.ak.rsm.measurement.Measurement;
import com.ak.rsm.medium.Layer1Medium;
import com.ak.rsm.medium.MediumLayers;
import com.ak.rsm.resistance.Resistivity;
import com.ak.rsm.resistance.TetrapolarResistance;
import com.ak.rsm.system.InexactTetrapolarSystem;
import com.ak.rsm.system.Layers;
import com.ak.util.Metrics;
import com.ak.util.Strings;
import org.apache.commons.math4.legacy.optim.PointValuePair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.lang.StrictMath.hypot;
import static java.lang.StrictMath.log;
import static org.assertj.core.api.Assertions.assertThat;

class Inverse2Test {
  private static final Logger LOGGER = LoggerFactory.getLogger(Inverse2Test.class);
  private static final Function<Collection<InexactTetrapolarSystem>, Regularization> REGULARIZATION_FUNCTION =
      Regularization.Interval.ZERO_MAX_LOG.of(1.0);

  static {
    LOGGER.info("{}", REGULARIZATION_FUNCTION);
  }

  @ParameterizedTest
  @MethodSource({
      "com.ak.rsm.inverse.InverseTestE8422akProvider#e8422_2023_05_25_14_04_43",
      "com.ak.rsm.inverse.InverseTestE8422akProvider#e8422_2023_05_25_14_05_51",
      "com.ak.rsm.inverse.InverseTestE8385akProvider#e8385_2023_05_15",
      "com.ak.rsm.inverse.InverseTestE8481yariProvider#e8481_2023_06_09",
  })
  @Disabled("ignored com.ak.rsm.inverse.Inverse2Test.testAlpha1")
  void testAlpha1(Collection<? extends DerivativeMeasurement> ms) {
    var medium = DynamicAbsolute.ofLayer2(ms, REGULARIZATION_FUNCTION);
    Assertions.assertNotNull(medium);
    LOGGER.info("\n{}", medium);
  }

  @ParameterizedTest
  @MethodSource({
      "com.ak.rsm.inverse.InverseTestE9682akProvider#e9682model",
      "com.ak.rsm.inverse.InverseTestE9682akProvider#e9682force",
  })
  @Disabled("ignored com.ak.rsm.inverse.Inverse2Test.findDeltaRho")
  void findDeltaRho(List<? extends Measurement> mRest, List<? extends Measurement> mForce) {
    assertThat(mRest).hasSameSizeAs(mForce);
    MediumLayers layer1Rest = new Layer1Medium(mRest);
    MediumLayers layer1Force = new Layer1Medium(mForce);
    LOGGER.atInfo()
        .log("\n{}{}\nrest  {}\nforce {}",
            Strings.CAP_DELTA,
            Strings.rho(2, "%.3f".formatted(layer1Force.rho().value() - layer1Rest.rho().value())),
            layer1Rest, layer1Force);
    Regularization regularization = REGULARIZATION_FUNCTION.apply(mRest.stream().map(Measurement::toInexact).toList());
    double baseL = Resistivity.getBaseL(mRest);
    for (double hmm = 1.0; hmm < 10.0; hmm += 0.5) {
      double h = Metrics.MILLI.applyAsDouble(hmm);
      PointValuePair optimized = Simplex.optimizeAll(point -> {
            double rho1 = point[0];
            double rho2 = point[1];
            double dRho2 = point[2];

            double e = 0.0;
            for (int i = 0, n = Math.min(mRest.size(), mForce.size()); i < n; i++) {
              double f = mForce.get(i).resistivity();
              double r = mRest.get(i).resistivity();
              double modelR = TetrapolarResistance.of(mRest.get(i).system()).rho1(rho1).rho2(rho2).h(h).resistivity();
              double modelF = TetrapolarResistance.of(mForce.get(i).system()).rho1(rho1).rho2(rho2 + dRho2).h(h).resistivity();
              e = hypot(log(r) - log(modelR), e);
              e = hypot(log(f - r) - log(modelF - modelR), e);
            }
            double regularizing = regularization.of(Layers.getK12(rho1, rho2), h / baseL);
            if (Double.isFinite(regularizing)) {
              return hypot(e, regularizing);
            }
            return regularizing;
          },
          new Simplex.Bounds(layer1Force.rho().value(), layer1Force.rho().value() * 3.0),
          new Simplex.Bounds(0.0, layer1Rest.rho().value()),
          new Simplex.Bounds(0.0, (layer1Force.rho().value() - layer1Rest.rho().value()) * 5.0)
      );
      LOGGER.atInfo()
          .addKeyValue("point", Arrays.stream(optimized.getPoint()).mapToObj("%.3f"::formatted).collect(Collectors.joining("; ")))
          .addKeyValue("h", hmm).log(optimized.getValue().toString());
    }
  }
}
