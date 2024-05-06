package com.ak.appliance.nmis.comm.bytes;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class NmisResponseFrameTest {
  @ParameterizedTest
  @MethodSource("com.ak.appliance.nmis.comm.bytes.NmisTestProvider#invalidTestByteResponse")
  void testNewInstance(ByteBuffer byteBuffer) {
    assertAll(Arrays.toString(byteBuffer.array()),
        () -> assertThat(NmisAddress.find(byteBuffer)).isNotEmpty(),
        () -> assertThat(NmisProtocolByte.checkCRC(byteBuffer)).isTrue(),
        () -> assertThat(new NmisResponseFrame.Builder(byteBuffer).build()).isEmpty());
  }

  @ParameterizedTest
  @MethodSource("com.ak.appliance.nmis.comm.bytes.NmisTestProvider#sequenceResponse")
  void testEquals(NmisRequest request, byte[] input) {
    NmisResponseFrame nmisResponseFrame = new NmisResponseFrame.Builder(ByteBuffer.wrap(input)).build().orElseThrow();
    assertThat(nmisResponseFrame).withFailMessage(nmisResponseFrame::toString)
        .isNotEqualTo(request).hasSameHashCodeAs(request.toResponse());
  }
}