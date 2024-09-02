package com.ak.fx.desktop;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class LoopbackViewControllerTest {
  @Test
  void testGet() {
    try (LoopbackViewController controller = new LoopbackViewController()) {
      controller.close();
      Assertions.assertThat(controller.get()).hasToString("BufferFrame[ 0xaa, 0x00, 0x00, 0x01, 0x00 ] 5 bytes");
      Assertions.assertThat(controller.get()).hasToString("BufferFrame[ 0xaa, 0x00, 0x00, 0x01, 0x00 ] 5 bytes");
    }
    catch (IOException e) {
      Assertions.fail(e.getMessage(), e);
    }
  }
}