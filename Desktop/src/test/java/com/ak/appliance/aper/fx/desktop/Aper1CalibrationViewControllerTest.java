package com.ak.appliance.aper.fx.desktop;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class Aper1CalibrationViewControllerTest {
  @Test
  void testCreate() {
    Assertions.assertThat(new Aper1CalibrationViewController()).isNotNull();
  }
}