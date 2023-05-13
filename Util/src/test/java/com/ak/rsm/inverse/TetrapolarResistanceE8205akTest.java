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
  static Stream<Arguments> e8205_18_06_48_00to17N() {
    double rho1 = 6.042;
    double rho2 = 5.872;
    double h = 2.5;

    double dRho1 = -0.113;
    double dRho2 = -0.08;
    double dHmm = -0.1;

    return Stream.of(
        arguments(
            TetrapolarResistance.milli().system2(7.0).rho1(rho1).rho2(rho2).h(h),
            new double[] {5.9246, 5.9126}
        ),
        arguments(
            TetrapolarResistance.milli().system2(7.0).rho1(rho1 + dRho1).rho2(rho2).h(h),
            new double[] {5.9246 - 0.0348, 5.9126 - 0.0268}
        ),
        arguments(
            TetrapolarResistance.milli().system2(7.0).rho1(rho1).rho2(rho2 + dRho2).h(h),
            new double[] {5.9246 - 0.0559, 5.9126 - 0.0614}
        ),
        arguments(
            TetrapolarResistance.milli().system2(7.0).rho1(rho1).rho2(rho2).h(h + dHmm),
            new double[] {5.9246 - 0.003, 5.9126 - 0.0024}
        )
    );
  }

  static Stream<Arguments> e8205_18_06_48_17to30N() {
    double rho1 = 5.929;
    double rho2 = 5.792;
    double h = 2.4;

    double dRho1 = -0.163;
    double dRho2 = -0.155;
    double dHmm = -0.1;
    return Stream.of(
        arguments(
            TetrapolarResistance.milli().system2(7.0).rho1(rho1).rho2(rho2).h(h),
            new double[] {5.8321, 5.8229}
        ),
        arguments(
            TetrapolarResistance.milli().system2(7.0).rho1(rho1 + dRho1).rho2(rho2).h(h),
            new double[] {5.8321 - 0.0478, 5.8229 - 0.0369}
        ),
        arguments(
            TetrapolarResistance.milli().system2(7.0).rho1(rho1).rho2(rho2 + dRho2).h(h),
            new double[] {5.8321 - 0.1111, 5.8229 - 0.1213}
        ),
        arguments(
            TetrapolarResistance.milli().system2(7.0).rho1(rho1).rho2(rho2).h(h + dHmm),
            new double[] {5.8321 - 0.0025, 5.8229 - 0.002}
        )
    );
  }

  static Stream<Arguments> e8205_18_06_48_30to48N() {
    double rho1 = 5.766;
    double rho2 = 5.637;
    double h = 2.3;

    double dRho1 = 0.135;
    double dRho2 = 0.213;
    double dHmm = -0.6;
    return Stream.of(
        arguments(
            TetrapolarResistance.milli().system2(7.0).rho1(rho1).rho2(rho2).h(h),
            new double[] {5.6724, 5.6643}
        ),
        arguments(
            TetrapolarResistance.milli().system2(7.0).rho1(rho1 + dRho1).rho2(rho2).h(h),
            new double[] {5.6724 + 0.0361, 5.6643 + 0.0276}
        ),
        arguments(
            TetrapolarResistance.milli().system2(7.0).rho1(rho1).rho2(rho2 + dRho2).h(h),
            new double[] {5.6724 + 0.154, 5.6643 + 0.1675}
        ),
        arguments(
            TetrapolarResistance.milli().system2(7.0).rho1(rho1).rho2(rho2).h(h + dHmm),
            new double[] {5.6724 - 0.0135, 5.6643 - 0.0107}
        ),

        arguments(
            TetrapolarResistance.milli().system2(7.0).rho1(rho1 + dRho1).rho2(rho2 + dRho2).h(h + dHmm),
            new double[] {5.8587, 5.8566}
        )
    );
  }

  @ParameterizedTest
  @MethodSource({"e8205_18_06_48_00to17N", "e8205_18_06_48_17to30N", "e8205_18_06_48_30to48N"})
  @ParametersAreNonnullByDefault
  void test(Collection<Resistance> ms, double[] resistivity) {
    assertThat(ms.stream().mapToDouble(Resistance::resistivity).toArray())
        .withFailMessage(ms::toString).containsExactly(resistivity, byLessThan(1.0e-4));
  }
}