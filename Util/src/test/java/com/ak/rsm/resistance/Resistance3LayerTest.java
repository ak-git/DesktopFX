package com.ak.rsm.resistance;

import com.ak.rsm.system.TetrapolarSystem;
import com.ak.util.Metrics;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.measure.MetricPrefix;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.byLessThan;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static tech.units.indriya.unit.Units.METRE;

class Resistance3LayerTest {
  private static final int SCALE = 2;

  static Stream<Arguments> threeLayerParameters() {
    return Stream.of(
        arguments(new double[] {2.0, 1.0, 4.0}, Metrics.Length.MILLI.to(1, METRE), new int[] {10, 1}, 10.0, 20.0, 86.866),
        arguments(new double[] {2.0, 1.0, 4.0}, Metrics.Length.MILLI.to(1, METRE), new int[] {10 + 1, 1 + 2}, 10.0, 20.0, 86.866 - 1.988)
    );
  }

  @ParameterizedTest
  @MethodSource({"threeLayerParameters"})
  void testLayer(double[] rho, double hStepSI, int[] p,
                 double smm, double lmm, double rOhm) {
    TetrapolarSystem system = new TetrapolarSystem(Metrics.Length.MILLI.to(smm, METRE), Metrics.Length.MILLI.to(lmm, METRE));
    assertAll(Arrays.toString(rho),
        () -> assertThat(new Resistance3Layer(system, hStepSI / SCALE)
            .value(rho[0], rho[1], rho[2], p[0] * SCALE, p[1] * SCALE))
            .isCloseTo(rOhm, byLessThan(0.001)),
        () -> assertThat(TetrapolarResistance
            .ofMilli(smm, lmm)
            .rho1(rho[0]).rho2(rho[1]).rho3(rho[2])
            .hStep(Metrics.Length.METRE.to(hStepSI, MetricPrefix.MILLI(METRE))).p(p[0], p[1]).ohms())
            .isCloseTo(rOhm, byLessThan(0.001))
    );
  }
}