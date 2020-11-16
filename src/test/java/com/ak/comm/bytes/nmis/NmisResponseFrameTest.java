package com.ak.comm.bytes.nmis;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.testng.Assert;
import org.testng.annotations.Test;

public class NmisResponseFrameTest {
  @Test(dataProviderClass = NmisTestProvider.class, dataProvider = "invalidTestByteResponse")
  public void testNewInstance(byte[] input) {
    ByteBuffer byteBuffer = ByteBuffer.wrap(input);
    Assert.assertNotNull(NmisAddress.find(byteBuffer), Arrays.toString(input));
    Assert.assertTrue(NmisProtocolByte.checkCRC(byteBuffer), Arrays.toString(input));
    Assert.assertNull(new NmisResponseFrame.Builder(byteBuffer).build(), Arrays.toString(input));
  }

  @Test(dataProviderClass = NmisTestProvider.class, dataProvider = "sequenceResponse")
  public void testEquals(NmisRequest request, byte[] input) {
    NmisResponseFrame nmisResponseFrame = new NmisResponseFrame.Builder(ByteBuffer.wrap(input)).build();
    Assert.assertNotNull(nmisResponseFrame);
    Assert.assertNotEquals(request, nmisResponseFrame, Arrays.toString(input));
    Assert.assertEquals(nmisResponseFrame, nmisResponseFrame, nmisResponseFrame.toString());
    Assert.assertEquals(request.toResponse().hashCode(), nmisResponseFrame.hashCode());
  }
}