package com.ak.fx.scene;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

class FontsTest {

  @Test
  void fontProperty() {
    Assertions.assertThat(Fonts.LOGO.fontProperty(() -> null).toString()).contains("Monospaced").contains("Bold");
    Assertions.assertThat(Fonts.LOGO_SMALL.fontProperty(() -> null).toString()).contains("Monospaced").contains("Bold");
    Assertions.assertThat(Fonts.H1.fontProperty(() -> null).toString()).contains("Tahoma").contains("Bold");
    Assertions.assertThat(Fonts.H2.fontProperty(() -> null).toString()).contains("Tahoma").contains("Regular");
  }

  @Test
  void values() {
    Assertions.assertThat(EnumSet.allOf(Fonts.class)).hasSize(4);
  }
}