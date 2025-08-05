package com.ak.rsm.resistance;

import com.ak.rsm.system.TetrapolarSystem;
import com.ak.util.Metrics;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.byLessThan;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static tech.units.indriya.unit.Units.METRE;

class Resistance1LayerTest {
  static Stream<Arguments> singleLayerParameters() {
    return Stream.of(
        arguments(1.0, 20.0, 40.0, 21.221),
        arguments(2.0, 40.0, 20.0, 21.221 * 2.0),
        arguments(1.0, 40.0, 80.0, 10.610),
        arguments(1.0, 80.0, 40.0, 10.610),

        arguments(0.7, 6.0 * 1, 6.0 * 3, 18.568),
        arguments(0.7, 6.0 * 3, 6.0 * 5, 27.852),
        arguments(0.7, 6.0 * 2, 6.0 * 4, 12.3785 * 2),
        arguments(0.7, 6.0 * 4, 6.0 * 6, 27.2325 * 2 - 12.3785 * 2),

        arguments(0.7, 7.0 * 1, 7.0 * 3, 15.915),
        arguments(0.7, 7.0 * 3, 7.0 * 5, 23.873),
        arguments(0.7, 7.0 * 2, 7.0 * 4, 10.6105 * 2),
        arguments(0.7, 7.0 * 4, 7.0 * 6, 23.343 * 2 - 10.6105 * 2),

        arguments(0.7, 8.0 * 1, 8.0 * 3, 13.926),
        arguments(0.7, 8.0 * 3, 8.0 * 5, 20.889),
        arguments(0.7, 8.0 * 2, 8.0 * 4, 9.284 * 2),
        arguments(0.7, 8.0 * 4, 8.0 * 6, 20.425 * 2 - 9.284 * 2)
    );
  }

  @ParameterizedTest
  @MethodSource("singleLayerParameters")
  void testOneLayer(double rho, double smm, double lmm, double rOhm) {
    TetrapolarSystem system = new TetrapolarSystem(Metrics.Length.MILLI.to(smm, METRE), Metrics.Length.MILLI.to(lmm, METRE));
    assertThat(new Resistance1Layer(system).value(rho)).isCloseTo(rOhm, byLessThan(0.001));
    assertThat(TetrapolarResistance.ofMilli(smm, lmm).rho(rho).ohms()).isCloseTo(rOhm, byLessThan(0.001));
  }
}