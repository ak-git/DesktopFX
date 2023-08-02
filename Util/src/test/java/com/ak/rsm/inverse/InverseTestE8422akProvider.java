package com.ak.rsm.inverse;

import com.ak.rsm.measurement.TetrapolarDerivativeMeasurement;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;

class InverseTestE8422akProvider {
  private InverseTestE8422akProvider() {
  }

  static Stream<Arguments> e8422_2023_05_25_14_04_43() {
    double smmBase = 7.0;
    return Stream.of(
        arguments(
            TetrapolarDerivativeMeasurement.milli(0.1).dh(0.150).system2(smmBase)
                .ofOhms(135.1687, 203.1126, 135.1687 + 0.2822034, 203.1126 + 0.5831683)
        ),
        arguments(
            TetrapolarDerivativeMeasurement.milli(0.1).dh(0.150).system2(smmBase)
                .ofOhms(137.0167, 207.4542, 137.0167 + 0.3620525, 207.4542 + 0.7709094)
        ),
        arguments(
            TetrapolarDerivativeMeasurement.milli(0.1).dh(0.150).system2(smmBase)
                .ofOhms(140.7461, 215.4297, 140.7461 + 0.4942724, 215.4297 + 0.9339182)
        )
    );
  }

  static Stream<Arguments> e8422_2023_05_25_14_05_51() {
    double smmBase = 7.0;
    return Stream.of(
        arguments(
            TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.150).system2(smmBase)
                .ofOhms(135.6058, 204.6435, 135.6058 - 0.3080655, 204.6435 - 0.6059549)
        ),
        arguments(
            TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.150).system2(smmBase)
                .ofOhms(131.8405, 196.0634, 131.8405 - 0.1119001, 196.0634 - 0.2741253)
        ),

        arguments(
            TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.150).system2(smmBase)
                .ofOhms(137.1783, 208.1717, 137.1783 - 0.3803713, 208.1717 - 0.7287852)
        ),
        arguments(
            TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.150).system2(smmBase)
                .ofOhms(131.5147, 194.4331, 131.5147 - 0.1291841, 194.4331 - 0.3076624)
        ),

        arguments(
            TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.150).system2(smmBase)
                .ofOhms(139.3341, 211.971, 139.3341 - 0.4176148, 211.971 - 0.8034943)
        ),
        arguments(
            TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.150).system2(smmBase)
                .ofOhms(133.5604, 199.4161, 133.5604 - 0.3103365, 199.4161 - 0.6054953)
        )
    );
  }
}
