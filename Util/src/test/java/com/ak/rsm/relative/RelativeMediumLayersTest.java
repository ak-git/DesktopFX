package com.ak.rsm.relative;

import com.ak.util.Strings;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RelativeMediumLayersTest {
  @Test
  void testSingleLayer() {
    assertThat(RelativeMediumLayers.SINGLE_LAYER.k12()).isZero();
    assertThat(RelativeMediumLayers.SINGLE_LAYER.k12AbsError()).isZero();
    assertThat(RelativeMediumLayers.SINGLE_LAYER.hToL()).isNaN();
    assertThat(RelativeMediumLayers.SINGLE_LAYER.hToLAbsError()).isZero();
    assertThat(RelativeMediumLayers.SINGLE_LAYER).hasToString(Strings.EMPTY);
  }

  @Test
  void testNaN() {
    assertThat(RelativeMediumLayers.NAN.k12()).isNaN();
    assertThat(RelativeMediumLayers.NAN.k12AbsError()).isZero();
    assertThat(RelativeMediumLayers.NAN.hToL()).isNaN();
    assertThat(RelativeMediumLayers.NAN.hToLAbsError()).isZero();
    assertThat(Double.toString(Double.NaN)).hasToString(RelativeMediumLayers.NAN.toString());
  }
}