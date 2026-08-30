package com.ak.fx.scene;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;

class FontsTest {

  @Test
  void fontProperty() {
    assertThat(Fonts.LOGO.fontProperty(() -> null).toString()).contains("Monospaced").contains("Bold");
    assertThat(Fonts.LOGO_SMALL.fontProperty(() -> null).toString()).contains("Monospaced").contains("Bold");
    assertThat(Fonts.H1.fontProperty(() -> null).toString()).containsAnyOf("Tahoma", "System").contains("Bold");
    assertThat(Fonts.H2.fontProperty(() -> null).toString()).containsAnyOf("Tahoma", "System").contains("Regular");
  }

  @Test
  void values() {
    assertThat(EnumSet.allOf(Fonts.class)).hasSize(4);
  }
}