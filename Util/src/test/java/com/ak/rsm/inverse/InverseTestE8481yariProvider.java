package com.ak.rsm.inverse;

import com.ak.rsm.measurement.TetrapolarDerivativeMeasurement;
import com.ak.rsm.resistance.DeltaH;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;

class InverseTestE8481yariProvider {
  private InverseTestE8481yariProvider() {
  }

  static Stream<Arguments> e8481_2023_06_09() {
    double smmBase = 8.0;
    double dhStepMM = 0.01;
    return DoubleStream.iterate(dhStepMM, value -> value < 0.1 + dhStepMM / 2.0, operand -> operand + dhStepMM)
        .mapToObj(dh -> arguments(
            TetrapolarDerivativeMeasurement.milli(0.1).dh(DeltaH.H1.apply(-dh)).system2(smmBase)
                .ofOhms(127.1, 206.8, 127.33, 207.16)
        ));
  }
}
