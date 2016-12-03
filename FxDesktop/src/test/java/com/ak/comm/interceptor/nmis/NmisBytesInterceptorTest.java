package com.ak.comm.interceptor.nmis;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

import com.ak.comm.bytes.nmis.NmisAddress;
import com.ak.comm.bytes.nmis.NmisRequest;
import com.ak.comm.bytes.nmis.NmisResponseFrame;
import com.ak.comm.bytes.nmis.NmisTestProvider;
import com.ak.comm.interceptor.BytesInterceptor;
import org.testng.Assert;
import org.testng.annotations.Test;

public final class NmisBytesInterceptorTest {
  @Test(dataProviderClass = NmisTestProvider.class, dataProvider = "allOhmsMyoOff")
  public void testRequestOhms(NmisRequest request, byte[] expected) {
    testRequest(request, expected);
  }

  @Test(dataProviderClass = NmisTestProvider.class, dataProvider = "360OhmsMyoHz")
  public void testRequestMyo(NmisRequest request, byte[] expected) {
    testRequest(request, expected);
  }

  @Test(dataProviderClass = NmisTestProvider.class, dataProvider = "sequence")
  public void testRequestSequence(NmisRequest request, byte[] expected) {
    testRequest(request, expected);
  }

  @Test(dataProviderClass = NmisTestProvider.class, dataProvider = "allOhmsMyoOffResponse")
  public void testResponseOhms(NmisRequest request, byte[] input) {
    Assert.assertEquals(request.toResponse(), new NmisResponseFrame.Builder(ByteBuffer.wrap(Arrays.copyOfRange(input, 1, input.length))).build());
    Assert.assertNotEquals(request.toResponse(), new NmisResponseFrame.Builder(ByteBuffer.wrap(input)).build());
    testResponse(request, input);
  }

  @Test(dataProviderClass = NmisTestProvider.class, dataProvider = "360OhmsMyoHzResponse")
  public void testResponseMyo(NmisRequest request, byte[] input) {
    Assert.assertEquals(request.toResponse(), new NmisResponseFrame.Builder(ByteBuffer.wrap(input)).build());
    testResponse(request, input);
  }

  @Test(dataProviderClass = NmisTestProvider.class, dataProvider = "sequenceResponse")
  public void testResponseSequence(NmisRequest request, byte[] input) {
    Assert.assertEquals(request.toResponse(), new NmisResponseFrame.Builder(ByteBuffer.wrap(input)).build());
    testResponse(request, input);
  }

  @Test(dataProviderClass = NmisTestProvider.class, dataProvider = "aliveAndChannelsResponse")
  public void testResponseAliveAndChannels(NmisAddress address, byte[] input) {
    if (NmisAddress.CHANNELS.contains(address)) {
      Optional.ofNullable(new NmisResponseFrame.Builder(ByteBuffer.wrap(input)).build()).orElseThrow(NullPointerException::new);
    }
  }

  @Test(dataProviderClass = NmisTestProvider.class, dataProvider = "invalidTestByteResponse")
  public void testInvalidResponse(byte[] input) {
    testResponse(NmisRequest.Sequence.CATCH_30.build(), input);
  }

  @Test(dataProviderClass = NmisTestProvider.class, dataProvider = "invalidCRCResponse")
  public void testInvalidResponseCRC(byte[] input) {
    testResponse(NmisRequest.Sequence.CATCH_30.build(), input);
  }

  private static void testRequest(NmisRequest request, byte[] expected) {
    ByteBuffer byteBuffer = ByteBuffer.allocate(expected.length);
    request.writeTo(byteBuffer);
    Assert.assertEquals(byteBuffer.array(), expected, request.toString());
    Assert.assertThrows(CloneNotSupportedException.class, request::clone);
  }

  private static void testResponse(NmisRequest request, byte[] input) {
    BytesInterceptor<NmisResponseFrame, NmisRequest> interceptor = new NmisBytesInterceptor();
    Collection<NmisResponseFrame> frames = interceptor.apply(ByteBuffer.wrap(input)).collect(Collectors.toList());

    if (!frames.isEmpty()) {
      Assert.assertEquals(frames, Collections.singleton(request.toResponse()));
    }
    Assert.assertTrue(interceptor.putOut(request).remaining() > 0);
  }
}