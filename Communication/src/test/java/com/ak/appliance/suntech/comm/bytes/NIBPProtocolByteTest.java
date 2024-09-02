package com.ak.appliance.suntech.comm.bytes;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NIBPProtocolByteTest {
  @Test
  void testRequestCRC() {
    assertThat(List.of(NIBPRequest.GET_BP_DATA, NIBPRequest.GET_CUFF_PRESSURE, NIBPRequest.START_BP))
        .isNotEmpty().allMatch(request -> {
          ByteBuffer byteBuffer = ByteBuffer.allocate(NIBPProtocolByte.MAX_CAPACITY);
          request.writeTo(byteBuffer);
          byteBuffer.rewind();
          return NIBPProtocolByte.checkCRC(byteBuffer);
        });
  }
}