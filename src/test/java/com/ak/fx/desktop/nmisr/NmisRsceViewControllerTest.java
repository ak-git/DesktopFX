package com.ak.fx.desktop.nmisr;

import com.ak.comm.bytes.nmis.NmisRequest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.EnumSet;

class NmisRsceViewControllerTest {
  @Test
  void testGet() {
    try (NmisRsceViewController controller = new NmisRsceViewController(event -> Assertions.fail(event.toString()))) {
      Assertions.assertThat(controller.get())
          .isIn(EnumSet.allOf(NmisRequest.Sequence.class).stream().map(NmisRequest.Sequence::build).toArray());
    }
    catch (IOException e) {
      Assertions.fail(e.getMessage(), e);
    }
  }
}