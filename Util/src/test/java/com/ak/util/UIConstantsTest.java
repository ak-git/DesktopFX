package com.ak.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UIConstantsTest {
  @Test
  void values() {
    assertThat(UIConstants.values()).isEmpty();
    assertThat(UIConstants.UI_DELAY_3SEC).hasSeconds(3);
  }
}