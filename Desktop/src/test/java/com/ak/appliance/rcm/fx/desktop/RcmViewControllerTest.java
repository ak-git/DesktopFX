package com.ak.appliance.rcm.fx.desktop;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RcmViewControllerTest {
  @Test
  void create() {
    assertThat(new RcmViewController()).isNotNull();
  }
}