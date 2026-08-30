package com.ak.fx.desktop;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

class LoopbackViewControllerTest {
  @Test
  void get() {
    assertThatNoException().isThrownBy(() -> {
      try (LoopbackViewController controller = new LoopbackViewController()) {
        controller.close();
        assertThat(controller.get()).hasToString("BufferFrame[ 0xaa, 0x00, 0x00, 0x01, 0x00 ] 5 bytes");
        assertThat(controller.get()).hasToString("BufferFrame[ 0xaa, 0x00, 0x00, 0x01, 0x00 ] 5 bytes");
      }
    });
  }
}