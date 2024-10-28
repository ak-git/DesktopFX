package com.ak.rsm.inverse;

import com.ak.rsm.measurement.TetrapolarDerivativeMeasurement;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;

class InverseTestE8385akProvider {
  private InverseTestE8385akProvider() {
  }

  static Stream<Arguments> e8385_2023_05_15() {
    double smmBase = 7.0;
    double dhStepMM = 0.01;
    return DoubleStream.iterate(dhStepMM, value -> value < 0.1 + dhStepMM / 2.0, operand -> operand + dhStepMM)
        .mapToObj(dh -> arguments(
            TetrapolarDerivativeMeasurement.milli(0.1).dh(-dh).system2(smmBase)
                .ofOhms(165.5, 258.6, 166.1, 259.4)
        ));
  }
}
