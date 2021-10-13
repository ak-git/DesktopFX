package com.ak.comm.converter.rsce;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Locale;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import com.ak.comm.bytes.rsce.RsceCommandFrame;
import com.ak.comm.bytes.rsce.RsceTestDataProvider;
import com.ak.comm.converter.Converter;
import com.ak.comm.log.LogTestUtils;
import org.testng.Assert;
import org.testng.annotations.Test;
import tec.uom.se.AbstractUnit;
import tec.uom.se.unit.MetricPrefix;
import tec.uom.se.unit.Units;

import static com.ak.comm.bytes.LogUtils.LOG_LEVEL_VALUES;

public class RsceConverterTest {
  private static final Logger LOGGER = Logger.getLogger(RsceConverter.class.getName());

  @Test(dataProviderClass = RsceTestDataProvider.class, dataProvider = "rheo12-info")
  public void testApply(@Nonnull byte[] bytes, @Nonnull int[] rDozenMilliOhms, @Nonnull int[] infoOnes) {
    RsceCommandFrame frame = new RsceCommandFrame.ResponseBuilder(ByteBuffer.wrap(bytes)).build();
    Assert.assertNotNull(frame);
    Assert.assertEquals(LogTestUtils.isSubstituteLogLevel(LOGGER, LOG_LEVEL_VALUES, () -> {
      Converter<RsceCommandFrame, RsceVariable> converter = new RsceConverter();
      Stream<int[]> stream = converter.apply(frame);
      stream.forEach(ints ->
          Assert.assertEquals(ints, IntStream.concat(IntStream.concat(Arrays.stream(rDozenMilliOhms), Arrays.stream(infoOnes)),
              IntStream.of(0, 0, 0)).toArray(), Arrays.toString(ints)));

    }, logRecord -> {
      for (int milliOhm : rDozenMilliOhms) {
        Assert.assertTrue(logRecord.getMessage().contains(String.format(Locale.getDefault(), "%,d", milliOhm)));
      }
      for (RsceVariable rsceVariable : RsceVariable.values()) {
        Assert.assertTrue(logRecord.getMessage().contains(rsceVariable.name()));
        if (EnumSet.of(RsceVariable.ACCELEROMETER, RsceVariable.FINGER_CLOSED).contains(rsceVariable)) {
          Assert.assertEquals(rsceVariable.getUnit(), AbstractUnit.ONE);
        }
        else if (EnumSet.of(RsceVariable.R1, RsceVariable.R2).contains(rsceVariable)) {
          Assert.assertEquals(rsceVariable.getUnit(), MetricPrefix.CENTI(Units.OHM));
        }
        else {
          Assert.assertEquals(rsceVariable.getUnit(), Units.PERCENT);
        }
      }
    }), rDozenMilliOhms.length > 0 && infoOnes.length > 0);
  }
}