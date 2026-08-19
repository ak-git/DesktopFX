package com.ak.fx.desktop;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class LoopbackViewControllerTest {
  @Test
  void testGet() {
    Assertions.assertThatNoException().isThrownBy(() -> {
      try (LoopbackViewController controller = new LoopbackViewController()) {
        controller.close();
        Assertions.assertThat(controller.get()).hasToString("BufferFrame[ 0xaa, 0x00, 0x00, 0x01, 0x00 ] 5 bytes");
        Assertions.assertThat(controller.get()).hasToString("BufferFrame[ 0xaa, 0x00, 0x00, 0x01, 0x00 ] 5 bytes");
      }
    });
  }
}