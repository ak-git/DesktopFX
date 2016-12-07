package com.ak.comm.converter.rsce;

import java.nio.ByteBuffer;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import com.ak.comm.bytes.rsce.RsceCommandFrame;
import com.ak.comm.bytes.rsce.RsceTestDataProvider;
import com.ak.comm.converter.Converter;
import org.testng.Assert;
import org.testng.annotations.Test;

public final class RsceConverterTest {
  @Test(dataProviderClass = RsceTestDataProvider.class, dataProvider = "rheo12-catch-rotate")
  public void testApply(@Nonnull byte[] bytes, int[] rDozenMilliOhms) {
    RsceCommandFrame frame = new RsceCommandFrame.ResponseBuilder(ByteBuffer.wrap(bytes)).build();
    Assert.assertNotNull(frame);
    Converter<RsceCommandFrame, RsceVariable> converter = new RsceConverter();
    Stream<int[]> stream = converter.apply(frame);
    if (rDozenMilliOhms.length == 0) {
      Assert.assertEquals(stream.count(), 0);
    }
    else {
      stream.forEach(ints -> Assert.assertEquals(ints, rDozenMilliOhms));
    }
  }
}