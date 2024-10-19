package com.ak.rsm.inverse;

import com.ak.rsm.measurement.DerivativeMeasurement;
import com.ak.rsm.system.InexactTetrapolarSystem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.function.Function;

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
    var medium = DynamicAbsolute.LAYER_2.apply(ms, REGULARIZATION_FUNCTION);
    Assertions.assertNotNull(medium);
    LOGGER.info("\n{}", medium);
  }
}
