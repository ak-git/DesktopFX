package com.ak.rsm.prediction;

import com.ak.rsm.relative.RelativeMediumLayers;
import com.ak.rsm.resistance.DeltaH;
import com.ak.rsm.resistance.TetrapolarDerivativeResistance;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.byLessThan;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class TetrapolarDerivativePredictionTest {
  static Stream<Arguments> predictions() {
    Prediction prediction1 = Predictions.of(
        TetrapolarDerivativeResistance.ofSI(10, 20).dh(DeltaH.NULL).rho(100, 10.0),
        new RelativeMediumLayers(0.5, 0.5), 10.0);
    Prediction prediction2 = Predictions.of(
        TetrapolarDerivativeResistance.ofSI(10, 20).dh(DeltaH.NULL).rho(100, 10.0),
        new RelativeMediumLayers(0.5, 0.5), 10.0);

    Prediction prediction3 = Predictions.of(
        TetrapolarDerivativeResistance.ofSI(20, 10).dh(DeltaH.NULL).rho(100, 10.0),
        new RelativeMediumLayers(0.5, 1.0), 10.0);
    return Stream.of(
        arguments(prediction1, prediction1, true),
        arguments(prediction1, prediction2, true),
        arguments(prediction1, prediction3, false),
        arguments(prediction1, new Object(), false),
        arguments(new Object(), prediction1, false)
    );
  }

  @ParameterizedTest
  @MethodSource("predictions")
  void testEquals(Object o1, Object o2, boolean equals) {
    assertAll("%s compared with %s".formatted(o1, o2),
        () -> assertThat(o1.equals(o2)).isEqualTo(equals),
        () -> assertThat(o1.hashCode() == o2.hashCode()).isEqualTo(equals)
    );
  }

  @Test
  void testPrediction() {
    Prediction prediction = Predictions.of(
        TetrapolarDerivativeResistance.ofSI(10, 20).dh(DeltaH.NULL)
            .rho(100, 101.0),
        new RelativeMediumLayers(1.0, 1.0), 10.0);
    assertAll(prediction.toString(),
        () -> assertThat(prediction.getPredicted()).isCloseTo(-0.723, byLessThan(0.001)),
        () -> assertThat(prediction.getInequalityL2()).containsExactly(new double[] {8.75, 140.766}, byLessThan(0.001))
    );
  }
}