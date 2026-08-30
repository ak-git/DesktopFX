package com.ak.appliance.briko.fx.desktop;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BrikoViewControllerTest {
  @Test
  void create() {
    assertThat(new BrikoViewController()).isNotNull();
  }
}