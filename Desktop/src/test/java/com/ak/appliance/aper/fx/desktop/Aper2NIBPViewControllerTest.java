package com.ak.appliance.aper.fx.desktop;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class Aper2NIBPViewControllerTest {
  @Test
  void testCreate() {
    Assertions.assertThat(new Aper2NIBPViewController()).isNotNull();
  }
}