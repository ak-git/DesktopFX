package com.ak.rsm.resistance;

import com.ak.util.Metrics;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.annotation.Nonnegative;
import java.util.Collection;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Percentage.withPercentage;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static tec.uom.se.unit.Units.METRE;

class ResistivityTest {

  static Stream<Arguments> resistivity() {
    return Stream.of(
        arguments(TetrapolarResistance.milli().system4(6.0)
            .ofOhms(100.0, 200.0, 300.0, 400.0), 6.0 * 4),
        arguments(TetrapolarDerivativeResistance.milli().dh(0.21).system2(7.0)
            .ofOhms(122.3, 199.0, 122.3 + 0.1, 199.0 + 0.4), 7.0 * 3)
    );
  }

  @ParameterizedTest
  @MethodSource("resistivity")
  void testGetBaseL(Collection<? extends Resistivity> resistivity, @Nonnegative double expectedBaseLMilli) {
    assertThat(Resistivity.getBaseL(resistivity)).isCloseTo(Metrics.Length.MILLI.to(expectedBaseLMilli, METRE), withPercentage(1.0));
  }
}