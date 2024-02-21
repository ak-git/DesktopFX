package com.ak.rsm.inverse;

import com.ak.rsm.measurement.DerivativeMeasurement;
import com.ak.rsm.system.InexactTetrapolarSystem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collection;
import java.util.function.Function;
import java.util.logging.Logger;

class Inverse2Test {
  private static final Logger LOGGER = Logger.getLogger(Inverse2Test.class.getName());

  @ParameterizedTest
  @MethodSource({
      "com.ak.rsm.inverse.InverseTestE8422akProvider#e8422_2023_05_25_14_04_43",
      "com.ak.rsm.inverse.InverseTestE8422akProvider#e8422_2023_05_25_14_05_51",
  })
  @Disabled("ignored com.ak.rsm.inverse.Inverse2Test.testAlpha1")
  void testAlpha1(Collection<DerivativeMeasurement> ms) {
    testSingle(ms, Regularization.Interval.ZERO_MAX.of(1.0));
  }

  private static void testSingle(Collection<? extends DerivativeMeasurement> ms,
                                 Function<Collection<InexactTetrapolarSystem>, Regularization> regularizationFunction) {
    LOGGER.info(regularizationFunction::toString);
    var medium = DynamicAbsolute.LAYER_2.apply(ms, regularizationFunction);
    Assertions.assertNotNull(medium);
    LOGGER.info(medium::toString);
  }
}
