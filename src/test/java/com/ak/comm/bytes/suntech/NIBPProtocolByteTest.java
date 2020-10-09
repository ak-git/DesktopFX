package com.ak.comm.bytes.suntech;

import java.nio.ByteBuffer;
import java.util.stream.Stream;

import org.testng.Assert;
import org.testng.annotations.Test;

public class NIBPProtocolByteTest {
  @Test
  public void testRequestCRC() {
    Assert.assertTrue(
        Stream.of(NIBPRequest.GET_BP_DATA, NIBPRequest.GET_CUFF_PRESSURE, NIBPRequest.START_BP)
            .allMatch(request -> {
              ByteBuffer byteBuffer = ByteBuffer.allocate(NIBPProtocolByte.MAX_CAPACITY);
              request.writeTo(byteBuffer);
              byteBuffer.rewind();
              return NIBPProtocolByte.checkCRC(byteBuffer);
            })
    );
  }
}