package com.ak.rsm.relative;

import com.ak.util.Strings;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class Layer1RelativeMediumTest {
  @Test
  void testSingleLayer() {
    assertThat(Layer1RelativeMedium.SINGLE_LAYER.k12()).isZero();
    assertThat(Layer1RelativeMedium.SINGLE_LAYER.k12AbsError()).isZero();
    assertThat(Layer1RelativeMedium.SINGLE_LAYER.hToL()).isNaN();
    assertThat(Layer1RelativeMedium.SINGLE_LAYER.hToLAbsError()).isZero();
    assertThat(Layer1RelativeMedium.SINGLE_LAYER).hasToString(Strings.EMPTY);
  }

  @Test
  void testNaN() {
    assertThat(Layer1RelativeMedium.NAN.k12()).isNaN();
    assertThat(Layer1RelativeMedium.NAN.k12AbsError()).isZero();
    assertThat(Layer1RelativeMedium.NAN.hToL()).isNaN();
    assertThat(Layer1RelativeMedium.NAN.hToLAbsError()).isZero();
    assertThat(Double.toString(Double.NaN)).hasToString(Layer1RelativeMedium.NAN.toString());
  }
}