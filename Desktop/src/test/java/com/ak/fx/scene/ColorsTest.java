package com.ak.fx.scene;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;


class ColorsTest {
  @Test
  void values() {
    assertThat(EnumSet.allOf(Colors.class)).isEmpty();
  }
}