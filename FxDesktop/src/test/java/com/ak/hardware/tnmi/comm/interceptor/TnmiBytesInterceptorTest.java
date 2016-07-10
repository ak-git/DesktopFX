package com.ak.hardware.tnmi.comm.interceptor;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Optional;

import org.testng.Assert;
import org.testng.annotations.Test;
import rx.observers.TestSubscriber;

public final class TnmiBytesInterceptorTest {
  @Test(dataProviderClass = TnmiTestProvider.class, dataProvider = "allOhmsMyoOff")
  public void testRequestOhms(TnmiRequest request, byte[] expected) {
    testRequest(request, expected);
  }

  @Test(dataProviderClass = TnmiTestProvider.class, dataProvider = "360OhmsMyoHz")
  public void testRequestMyo(TnmiRequest request, byte[] expected) {
    testRequest(request, expected);
  }

  @Test(dataProviderClass = TnmiTestProvider.class, dataProvider = "sequence")
  public void testRequestSequence(TnmiRequest request, byte[] expected) {
    testRequest(request, expected);
  }

  @Test(dataProviderClass = TnmiTestProvider.class, dataProvider = "allOhmsMyoOffResponse")
  public void testResponseOhms(TnmiRequest request, byte[] input) {
    Assert.assertEquals(request.toResponse(), TnmiResponseFrame.newInstance(Arrays.copyOfRange(input, 1, input.length)));
    Assert.assertNotEquals(request.toResponse(), TnmiResponseFrame.newInstance(input));
    testResponse(request, input);
  }

  @Test(dataProviderClass = TnmiTestProvider.class, dataProvider = "360OhmsMyoHzResponse")
  public void testResponseMyo(TnmiRequest request, byte[] input) {
    Assert.assertEquals(request.toResponse(), TnmiResponseFrame.newInstance(input));
    testResponse(request, input);
  }

  @Test(dataProviderClass = TnmiTestProvider.class, dataProvider = "sequenceResponse")
  public void testResponseSequence(TnmiRequest request, byte[] input) {
    Assert.assertEquals(request.toResponse(), TnmiResponseFrame.newInstance(input));
    testResponse(request, input);
  }

  @Test(dataProviderClass = TnmiTestProvider.class, dataProvider = "aliveAndChannelsResponse")
  public void testResponseAliveAndChannels(TnmiAddress address, byte[] input) {
    if (TnmiAddress.CHANNELS.contains(address)) {
      Optional.ofNullable(TnmiResponseFrame.newInstance(input)).orElseThrow(NullPointerException::new);
      testResponse(TnmiRequest.Sequence.ROTATE_100.build(), input);
    }
  }

  @Test(dataProviderClass = TnmiTestProvider.class, dataProvider = "invalidTestByteResponse")
  public void testInvalidResponse(byte[] input) {
    testResponse(TnmiRequest.Sequence.CATCH_30.build(), input);
  }

  @Test(dataProviderClass = TnmiTestProvider.class, dataProvider = "invalidCRCResponse")
  public void testInvalidResponseCRC(byte[] input) {
    testResponse(TnmiRequest.Sequence.CATCH_30.build(), input);
  }

  private static void testRequest(TnmiRequest request, byte[] expected) {
    ByteBuffer byteBuffer = ByteBuffer.allocate(expected.length);
    request.writeTo(byteBuffer);
    Assert.assertEquals(byteBuffer.array(), expected, request.toString());
    Assert.assertThrows(CloneNotSupportedException.class, request::clone);
  }

  private static void testResponse(TnmiRequest request, byte[] input) {
    TnmiBytesInterceptor interceptor = new TnmiBytesInterceptor();
    TestSubscriber<TnmiResponseFrame> subscriber = TestSubscriber.create();
    interceptor.getBufferObservable().subscribe(subscriber);

    int countResponses = interceptor.write(ByteBuffer.wrap(input));
    if (countResponses == 0) {
      subscriber.assertNoValues();
    }
    else {
      Assert.assertEquals(countResponses, 1);
      subscriber.assertValue(request.toResponse());
    }
    Assert.assertTrue(interceptor.put(request).remaining() > 0);
    interceptor.close();
    subscriber.assertCompleted();
    subscriber.assertNoErrors();
  }
}