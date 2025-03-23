package com.ak.rsm.inverse;

import com.ak.rsm.measurement.DerivativeMeasurement;
import com.ak.rsm.medium.Layer2Medium;
import com.ak.rsm.relative.RelativeMediumLayers;
import com.ak.rsm.system.InexactTetrapolarSystem;
import com.ak.util.Metrics;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.measure.MetricPrefix;
import java.util.Collection;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static tech.units.indriya.unit.Units.METRE;

class Inverse2Test {
  private static final Logger LOGGER = LoggerFactory.getLogger(Inverse2Test.class);
  private static final Function<Collection<InexactTetrapolarSystem>, Regularization> REGULARIZATION_FUNCTION =
      Regularization.Interval.ZERO_MAX_LOG1P.of(1.0);

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
    var medium = DynamicAbsolute.of(ms, REGULARIZATION_FUNCTION);
    Assertions.assertNotNull(medium);
    LOGGER.info("\n{}", medium);
  }

  @ParameterizedTest
  @MethodSource({
      "com.ak.rsm.inverse.InverseTestE9625akProvider#e1",
  })
  @Disabled("ignored com.ak.rsm.inverse.Inverse2Test.testDouble")
  void testDouble(Collection<? extends DerivativeMeasurement> dm, Collection<? extends DerivativeMeasurement> dm2) {
    for (double d : new double[] {1.0}) {
      assertThatNoException().isThrownBy(() -> {
        Regularization.Interval regularization = Regularization.Interval.ZERO_MAX_LOG1P;
        Function<Collection<InexactTetrapolarSystem>, Regularization> regularizationFunction = regularization.of(d);
        LOGGER.info("{}", regularizationFunction);

        RelativeMediumLayers relativeMediumLayers = Relative.Dynamic.solve(dm, regularizationFunction);

        Layer2Medium layer2 = DynamicAbsolute.of(dm2, relativeMediumLayers);
        LOGGER.info("{}", layer2);

        Layer2Medium layer2Medium = DynamicAbsolute.of(dm, relativeMediumLayers);
        LOGGER.info("{}", layer2Medium);
        Layer2Medium layer2Medium2 = DynamicAbsolute.of(dm2, regularizationFunction);
        LOGGER.info("{}", layer2Medium2);
        LOGGER.info("diff h: {} mm", Metrics.Length.METRE.to(layer2Medium2.h().value() - layer2Medium.h().value(), MetricPrefix.MILLI(METRE)));
      });
    }
  }
}
