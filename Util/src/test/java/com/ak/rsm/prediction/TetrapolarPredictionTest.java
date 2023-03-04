package com.ak.rsm.prediction;

import com.ak.rsm.relative.Layer2RelativeMedium;
import com.ak.rsm.relative.RelativeMediumLayers;
import com.ak.rsm.resistance.Resistivity;
import com.ak.rsm.system.TetrapolarSystem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.byLessThan;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class TetrapolarPredictionTest {
  static Stream<Arguments> predictions() {
    Prediction prediction1 = TetrapolarPrediction.of(
        new Resistivity() {
          @Override
          public double resistivity() {
            return 100.0;
          }

          @Nonnull
          @Override
          public TetrapolarSystem system() {
            return new TetrapolarSystem(10.0, 20.0);
          }
        },
        new Layer2RelativeMedium(0.5, 0.5), 10.0);
    Prediction prediction2 = TetrapolarPrediction.of(
        new Resistivity() {
          @Override
          public double resistivity() {
            return 100.0;
          }

          @Nonnull
          @Override
          public TetrapolarSystem system() {
            return new TetrapolarSystem(20.0, 10.0);
          }
        },
        new Layer2RelativeMedium(0.5, 1.0), 10.0);

    return Stream.of(
        arguments(prediction1, prediction1, true),
        arguments(prediction1, prediction2, true),
        arguments(prediction1, new Object(), false),
        arguments(new Object(), prediction1, false)
    );
  }

  @ParameterizedTest
  @MethodSource("predictions")
  @ParametersAreNonnullByDefault
  void testEquals(Object o1, Object o2, boolean equals) {
    assertAll("%s compared with %s".formatted(o1, o2),
        () -> assertThat(o1.equals(o2)).isEqualTo(equals),
        () -> assertThat(o1.hashCode() == o2.hashCode()).isEqualTo(equals)
    );
    assertThat(o1).isNotEqualTo(null);
  }

  @Test
  void testPrediction() {
    Prediction prediction = TetrapolarPrediction.of(
        new Resistivity() {
          @Override
          public double resistivity() {
            return 100.0;
          }

          @Nonnull
          @Override
          public TetrapolarSystem system() {
            return new TetrapolarSystem(10.0, 20.0);
          }
        },
        RelativeMediumLayers.SINGLE_LAYER, 10.0);
    assertAll(prediction.toString(),
        () -> assertThat(prediction.getPredicted()).isCloseTo(10.0, byLessThan(0.001)),
        () -> assertThat(prediction.getInequalityL2()).containsExactly(new double[] {9.0}, byLessThan(0.001))
    );
  }
}