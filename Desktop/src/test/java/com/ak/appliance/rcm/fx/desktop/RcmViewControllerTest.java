package com.ak.appliance.rcm.fx.desktop;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class RcmViewControllerTest {
  @Test
  void testCreate() {
    Assertions.assertThat(new RcmViewController()).isNotNull();
  }
}