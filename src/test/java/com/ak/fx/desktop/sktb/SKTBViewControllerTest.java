package com.ak.fx.desktop.sktb;

import com.ak.comm.converter.rsce.RsceVariable;
import com.ak.fx.desktop.nmisr.RsceEvent;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.EnumSet;
import java.util.random.RandomGenerator;

class SKTBViewControllerTest {
  private static final RandomGenerator RANDOM = new SecureRandom();

  @Test
  void testGet() {
    try (SKTBViewController controller = new SKTBViewController()) {
      controller.close();
      controller.escape();
      controller.left();
      controller.right();
      controller.up();
      controller.down();
      controller.onApplicationEvent(new RsceEvent(this,
          EnumSet.allOf(RsceVariable.class).stream().mapToInt(value -> RANDOM.nextInt()).toArray()));
      Assertions.assertThat(controller.get())
          .hasToString("SKTBRequest[ 0x5a, 0x01, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xf4, 0x01 ] 11 bytes");
      Assertions.assertThat(controller.get())
          .hasToString("SKTBRequest[ 0x5a, 0x02, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xf4, 0x01 ] 11 bytes");
    }
    catch (IOException e) {
      Assertions.fail(e.getMessage(), e);
    }
  }
}