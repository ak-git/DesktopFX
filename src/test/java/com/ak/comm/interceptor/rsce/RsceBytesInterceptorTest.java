package com.ak.comm.interceptor.rsce;

import java.nio.ByteBuffer;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import com.ak.comm.bytes.LogUtils;
import com.ak.comm.bytes.rsce.RsceCommandFrame;
import com.ak.comm.bytes.rsce.RsceTestDataProvider;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.comm.log.LogTestUtils;
import com.ak.util.Strings;
import org.testng.Assert;
import org.testng.annotations.Test;

import static com.ak.comm.bytes.rsce.RsceCommandFrame.Control.CATCH;
import static com.ak.comm.bytes.rsce.RsceCommandFrame.RequestType.STATUS_I_SPEED_ANGLE;

public class RsceBytesInterceptorTest {
  private static final Logger LOGGER = Logger.getLogger(RsceBytesInterceptor.class.getName());

  @Test(dataProviderClass = RsceTestDataProvider.class, dataProvider = "simpleRequests")
  public void testSimpleRequest(@Nonnull byte[] bytes, @Nonnull RsceCommandFrame.Control control, @Nonnull RsceCommandFrame.RequestType type) {
    checkResponse(bytes, RsceCommandFrame.simple(control, type));
  }

  @Test(dataProviderClass = RsceTestDataProvider.class, dataProvider = "offRequests")
  public void testOffRequest(@Nonnull byte[] expected, @Nonnull RsceCommandFrame.Control control) {
    checkResponse(expected, RsceCommandFrame.off(control));
  }

  @Test(dataProviderClass = RsceTestDataProvider.class, dataProvider = "preciseRequests")
  public void testPreciseRequest(@Nonnull byte[] expected, short speed) {
    checkResponse(expected, RsceCommandFrame.precise(CATCH, STATUS_I_SPEED_ANGLE, speed));
  }

  private static void checkResponse(@Nonnull byte[] bytes, @Nonnull RsceCommandFrame request) {
    BytesInterceptor<RsceCommandFrame, RsceCommandFrame> interceptor = new RsceBytesInterceptor();

    LogTestUtils.isSubstituteLogLevel(LOGGER, LogUtils.LOG_LEVEL_LEXEMES,
        () -> Assert.assertEquals(interceptor.apply(ByteBuffer.wrap(bytes)).iterator(), Stream.of(request).iterator()),
        logRecord -> Assert.assertEquals(logRecord.getMessage().replaceAll(".*" + RsceCommandFrame.class.getSimpleName(), Strings.EMPTY),
            request.toString().replaceAll(".*" + RsceCommandFrame.class.getSimpleName(), Strings.EMPTY)));

    LogTestUtils.isSubstituteLogLevel(LOGGER, LogUtils.LOG_LEVEL_LEXEMES,
        () -> Assert.assertTrue(interceptor.putOut(request).remaining() > 0),
        logRecord -> Assert.assertEquals(logRecord.getMessage().replaceAll(".*" + RsceCommandFrame.class.getSimpleName(), Strings.EMPTY),
            request.toString().replaceAll(".*" + RsceCommandFrame.class.getSimpleName(), Strings.EMPTY) +
                " - " + bytes.length + " bytes OUT to hardware"));
  }
}