package com.ak.appliance.nmisr.fx.desktop;

import com.ak.appliance.nmis.comm.bytes.NmisRequest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.IntStream;

class NmisRsceViewControllerTest {
  @Test
  void testGet() {
    try (NmisRsceViewController controller = new NmisRsceViewController(event -> Assertions.fail(event.toString()))) {
      controller.close();
      List<NmisRequest> first6 = IntStream.range(0, 6).mapToObj(value -> controller.get()).toList();
      List<NmisRequest> second6 = IntStream.range(0, 6).mapToObj(value -> controller.get()).toList();
      Assertions.assertThat(first6).isEqualTo(second6)
          .isSubsetOf(EnumSet.allOf(NmisRequest.Sequence.class).stream().map(NmisRequest.Sequence::build).toList());
    }
    catch (IOException e) {
      Assertions.fail(e.getMessage(), e);
    }
  }
}