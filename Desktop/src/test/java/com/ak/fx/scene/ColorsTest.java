package com.ak.fx.scene;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;


class ColorsTest {
  @Test
  void values() {
    Assertions.assertThat(EnumSet.allOf(Colors.class)).isEmpty();
  }
}