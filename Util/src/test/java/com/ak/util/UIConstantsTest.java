package com.ak.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UIConstantsTest {
  @Test
  void testValues() {
    assertThat(UIConstants.values()).isEmpty();
    assertThat(UIConstants.UI_DELAY.getSeconds()).isEqualTo(3);
  }
}