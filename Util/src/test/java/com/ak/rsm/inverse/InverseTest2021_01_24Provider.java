package com.ak.rsm.inverse;

import com.ak.rsm.measurement.TetrapolarDerivativeMeasurement;
import org.junit.jupiter.params.provider.Arguments;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;

class InverseTest2021_01_24Provider {
  private InverseTest2021_01_24Provider() {
  }

  /**
   * <b>2021-01-24 7 мм 0.15 ak</b>
   *
   * @return s2
   */
  static Stream<Arguments> ak7_015() {
    double r1 = 138.7;
    double r2 = 217.0;
    double smmBase = 7.0;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.15).system2(smmBase)
                    .ofOhms(r1, r2, r1 - 0.2, r2 - 0.4)
            )
        )
    );
  }
}
