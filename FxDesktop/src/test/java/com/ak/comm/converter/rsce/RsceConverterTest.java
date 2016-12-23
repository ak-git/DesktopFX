package com.ak.comm.converter.rsce;

import java.nio.ByteBuffer;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import com.ak.comm.bytes.rsce.RsceCommandFrame;
import com.ak.comm.bytes.rsce.RsceTestDataProvider;
import com.ak.comm.converter.Converter;
import com.ak.comm.util.LogUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import static com.ak.comm.util.LogUtils.LOG_LEVEL_VALUES;

public final class RsceConverterTest {
  private static final Logger LOGGER = Logger.getLogger(RsceConverter.class.getName());

  @Test(dataProviderClass = RsceTestDataProvider.class, dataProvider = "rheo12-catch-rotate")
  public void testApply(@Nonnull byte[] bytes, int[] rDozenMilliOhms) {
    RsceCommandFrame frame = new RsceCommandFrame.ResponseBuilder(ByteBuffer.wrap(bytes)).build();
    Assert.assertNotNull(frame);
    LogUtils.substituteLogLevel(LOGGER, LOG_LEVEL_VALUES, () -> {
      Converter<RsceCommandFrame, RsceVariable> converter = new RsceConverter();
      Stream<int[]> stream = converter.apply(frame);
      if (rDozenMilliOhms.length == 0) {
        Assert.assertEquals(stream.count(), 0);
      }
      else {
        stream.forEach(ints -> Assert.assertEquals(ints, rDozenMilliOhms));
      }
    }, logRecord -> {
      for (int milliOhm : rDozenMilliOhms) {
        Assert.assertTrue(logRecord.getMessage().contains(Integer.toString(milliOhm)));
      }
      for (RsceVariable rsceVariable : RsceVariable.values()) {
        Assert.assertTrue(logRecord.getMessage().contains(rsceVariable.name()));
      }
    });
  }
}