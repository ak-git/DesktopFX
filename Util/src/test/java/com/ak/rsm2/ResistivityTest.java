package com.ak.rsm2;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class ResistivityTest {
  @Test
  void of() {
    ElectrodeSystem.Tetrapolar tetrapolar = ElectrodeSystem.ofMilli().tetrapolar(10.0, 20.0).build();
    double value = Resistivity.of(tetrapolar).value(K.of(8.0, 1.0), Phi.of(0.01, tetrapolar));
    Assertions.assertThat(value).isCloseTo(0.911, Assertions.byLessThan(0.001));
  }

  @Test
  void ofInv() {
    ElectrodeSystem.Tetrapolar tetrapolar = ElectrodeSystem.ofMilli().tetrapolar(20.0, 10.0).build();
    double value = Resistivity.of(tetrapolar).value(K.of(8.0, 1.0), Phi.of(0.01, tetrapolar));
    Assertions.assertThat(value).isCloseTo(0.911, Assertions.byLessThan(0.001));
  }
}