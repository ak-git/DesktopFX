package com.ak.comm.interceptor.nmis;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.ak.comm.bytes.nmis.NmisAddress;
import com.ak.comm.bytes.nmis.NmisRequest;
import com.ak.comm.bytes.nmis.NmisResponseFrame;
import com.ak.comm.bytes.nmis.NmisTestProvider;
import com.ak.comm.core.LogUtils;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.comm.log.LogTestUtils;
import com.ak.util.Strings;
import org.testng.Assert;
import org.testng.annotations.Test;

public class NmisBytesInterceptorTest {
  private static final Logger LOGGER = Logger.getLogger(NmisBytesInterceptor.class.getName());

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
    testResponse(request, input, true);
  }

  @Test(dataProviderClass = NmisTestProvider.class, dataProvider = "360OhmsMyoHzResponse")
  public void testResponseMyo(NmisRequest request, byte[] input) {
    Assert.assertEquals(request.toResponse(), new NmisResponseFrame.Builder(ByteBuffer.wrap(input)).build());
    testResponse(request, input, true);
  }

  @Test(dataProviderClass = NmisTestProvider.class, dataProvider = "sequenceResponse")
  public void testResponseSequence(NmisRequest request, byte[] input) {
    Assert.assertEquals(request.toResponse(), new NmisResponseFrame.Builder(ByteBuffer.wrap(input)).build());
    testResponse(request, input, true);
  }

  @Test(dataProviderClass = NmisTestProvider.class, dataProvider = "aliveAndChannelsResponse")
  public void testResponseAliveAndChannels(NmisAddress address, byte[] input) {
    if (NmisAddress.CHANNELS.contains(address)) {
      Assert.assertNotNull(Optional.ofNullable(new NmisResponseFrame.Builder(ByteBuffer.wrap(input)).build()).orElseThrow(NullPointerException::new));
    }
  }

  @Test(dataProviderClass = NmisTestProvider.class, dataProvider = "invalidTestByteResponse")
  public void testInvalidResponse(byte[] input) {
    testResponse(NmisRequest.Sequence.CATCH_30.build(), input, false);
  }

  @Test(dataProviderClass = NmisTestProvider.class, dataProvider = "invalidCRCResponse")
  public void testInvalidResponseCRC(byte[] input) {
    testResponse(NmisRequest.Sequence.CATCH_30.build(), input, false);
  }

  private static void testRequest(NmisRequest request, byte[] expected) {
    ByteBuffer byteBuffer = ByteBuffer.allocate(expected.length);
    request.writeTo(byteBuffer);
    Assert.assertEquals(byteBuffer.array(), expected, request.toString());
  }

  private static void testResponse(NmisRequest request, byte[] input, boolean logFlag) {
    BytesInterceptor<NmisRequest, NmisResponseFrame> interceptor = new NmisBytesInterceptor();

    Assert.assertEquals(LogTestUtils.isSubstituteLogLevel(LOGGER, LogUtils.LOG_LEVEL_LEXEMES, () -> {
      Collection<NmisResponseFrame> frames = interceptor.apply(ByteBuffer.wrap(input)).collect(Collectors.toList());
      if (!frames.isEmpty()) {
        Assert.assertEquals(frames, Collections.singleton(request.toResponse()));
      }
    }, logRecord -> Assert.assertEquals(logRecord.getMessage().replaceAll(".*" + NmisResponseFrame.class.getSimpleName(), Strings.EMPTY),
        request.toResponse().toString().replaceAll(".*" + NmisResponseFrame.class.getSimpleName(), Strings.EMPTY))), logFlag);

    AtomicReference<String> logMessage = new AtomicReference<>(Strings.EMPTY);
    Assert.assertTrue(LogTestUtils.isSubstituteLogLevel(LOGGER, LogUtils.LOG_LEVEL_ERRORS,
        () -> {
          int bytesOut = interceptor.putOut(request).remaining();
          Assert.assertTrue(bytesOut > 0);
          Assert.assertEquals(logMessage.get(),
              request.toString().replaceAll(".*" + NmisRequest.class.getSimpleName(), Strings.EMPTY) +
                  " - " + bytesOut + " bytes OUT to hardware");
        },
        logRecord -> logMessage.set(logRecord.getMessage().replaceAll(".*" + NmisRequest.class.getSimpleName(), Strings.EMPTY))));
  }
}