package com.ak.rsm.inverse;

import com.ak.rsm.resistance.Resistance;
import com.ak.rsm.resistance.TetrapolarResistance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.byLessThan;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class TetrapolarResistanceE8205akTest {
  static Stream<Arguments> e8205_18_06_48_00to15N() {
    double dRho1 = -0.026;
    double dRho2 = -0.003;
    double dHmm = -0.1;
    return Stream.of(
        arguments(
            TetrapolarResistance.milli().system2(7.0).rho1(5.930).rho2(5.794).h(2.6),
            new double[] {5.8386, 5.8285}
        ),
        arguments(
            TetrapolarResistance.milli().system2(7.0).rho1(5.930 + dRho1).rho2(5.794).h(2.6),
            new double[] {5.8386 - 0.0085, 5.8285 - 0.0066}
        ),
        arguments(
            TetrapolarResistance.milli().system2(7.0).rho1(5.930).rho2(5.794 + dRho2).h(2.6),
            new double[] {5.8386 - 0.0020, 5.8285 - 0.0022}
        ),
        arguments(
            TetrapolarResistance.milli().system2(7.0).rho1(5.930).rho2(5.794).h(2.6 + dHmm),
            new double[] {5.8386 - 0.0024, 5.8285 - 0.0019}
        )
    );
  }

  static Stream<Arguments> e8205_18_06_48_15to28N() {
    double dRho1 = -0.084;
    double dRho2 = -0.051;
    double dHmm = -0.3;
    return Stream.of(
        arguments(
            TetrapolarResistance.milli().system2(7.0).rho1(5.930 - 0.026).rho2(5.794 - 0.003).h(2.6 - 0.1),
            new double[] {5.8261, 5.8181}
        ),
        arguments(
            TetrapolarResistance.milli().system2(7.0).rho1(5.930 - 0.026 + dRho1).rho2(5.794 - 0.003).h(2.6 - 0.1),
            new double[] {5.8261 - 0.0260, 5.8181 - 0.0201}
        ),
        arguments(
            TetrapolarResistance.milli().system2(7.0).rho1(5.930 - 0.026).rho2(5.794 - 0.003 + dRho2).h(2.6 - 0.1),
            new double[] {5.8261 - 0.0354, 5.8181 - 0.0389}
        ),
        arguments(
            TetrapolarResistance.milli().system2(7.0).rho1(5.930 - 0.026).rho2(5.794 - 0.003).h(2.6 - 0.1 + dHmm),
            new double[] {5.8261 - 0.006, 5.8181 - 0.0048}
        )
    );
  }

  static Stream<Arguments> e8205_18_06_48_28to48N() {
    double dRho1 = 0.079;
    double dRho2 = 0.107;
    double dHmm = -0.3;
    return Stream.of(
        arguments(
            TetrapolarResistance.milli().system2(7.0)
                .rho1(5.930 - 0.026 - 0.084).rho2(5.794 - 0.003 - 0.051).h(2.6 - 0.1 - 0.3),
            new double[] {5.7607, 5.7559}
        ),
        arguments(
            TetrapolarResistance.milli().system2(7.0)
                .rho1(5.930 - 0.026 - 0.084 + dRho1).rho2(5.794 - 0.003 - 0.051).h(2.6 - 0.1 - 0.3),
            new double[] {5.7607 + 0.02, 5.7559 + 0.0154}
        ),
        arguments(
            TetrapolarResistance.milli().system2(7.0)
                .rho1(5.930 - 0.026 - 0.084).rho2(5.794 - 0.003 - 0.051 + dRho2).h(2.6 - 0.1 - 0.3),
            new double[] {5.7607 + 0.0792, 5.7559 + 0.0857}
        ),
        arguments(
            TetrapolarResistance.milli().system2(7.0)
                .rho1(5.930 - 0.026 - 0.084).rho2(5.794 - 0.003 - 0.051).h(2.6 - 0.1 - 0.3 + dHmm),
            new double[] {5.7607 - 0.0043, 5.7559 - 0.0034}
        )
    );
  }

  @ParameterizedTest
  @MethodSource({"e8205_18_06_48_00to15N", "e8205_18_06_48_15to28N", "e8205_18_06_48_28to48N"})
  @ParametersAreNonnullByDefault
  void test(Collection<Resistance> ms, double[] resistivity) {
    assertThat(ms.stream().mapToDouble(Resistance::resistivity).toArray())
        .withFailMessage(ms::toString).containsExactly(resistivity, byLessThan(1.0e-4));
  }
}