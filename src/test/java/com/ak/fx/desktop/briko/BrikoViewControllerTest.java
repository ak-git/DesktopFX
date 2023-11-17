package com.ak.fx.desktop.briko;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class BrikoViewControllerTest {
  @Test
  void testCreate() {
    Assertions.assertThat(new BrikoViewController()).isNotNull();
  }
}