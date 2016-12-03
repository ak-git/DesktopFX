package com.ak.comm.interceptor.nmisr;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Collections;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.ak.comm.bytes.nmis.NmisProtocolByte;
import com.ak.comm.bytes.nmis.NmisRequest;
import com.ak.comm.bytes.rsce.RsceCommandFrame;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.comm.interceptor.nmis.NmisBytesInterceptor;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public final class NmisRsceBytesInterceptorTest {
  private final BytesInterceptor<RsceCommandFrame, NmisRequest> interceptor = new NmisRsceBytesInterceptor();
  private final ByteBuffer byteBuffer = ByteBuffer.allocate(NmisProtocolByte.MAX_CAPACITY);

  @DataProvider(name = "data")
  public Object[][] data() {
    return new Object[][] {
        new Object[] {new byte[] {
            // NO Data, empty Rsce frame
            0x7e, 0x45, 0x02, 0x3f, 0x00, 0x04},
            RsceCommandFrame.simple(RsceCommandFrame.Control.ALL, RsceCommandFrame.RequestType.EMPTY)},

        new Object[] {new byte[] {
            0x7e, 0x45, 0x08, 0x3f, 0x00, 0x03, 0x04, 0x18, 0x32, (byte) 0xca, 0x74, (byte) 0x99},
            RsceCommandFrame.position(RsceCommandFrame.Control.ROTATE, (byte) 50)},

        new Object[] {new byte[] {
            0x7e, 0x45, 0x09, 0x44, 0x00, 0x01, 0x05, 0x0b, (byte) 0xe0, (byte) 0xb1, (byte) 0xe1, 0x7a, 0x0d},
            RsceCommandFrame.precise(RsceCommandFrame.Control.CATCH, RsceCommandFrame.RequestType.STATUS_I_ANGLE)},

        new Object[] {new byte[] {
            // NO Data, invalid Rsce frame
            0x7e, (byte) 0x92, 0x08, 0x01, 0x00, 0x00, 0x00, (byte) 0x84, (byte) 0x84, (byte) 0x84, (byte) 0x84, 0x29},
            RsceCommandFrame.simple(RsceCommandFrame.Control.ALL, RsceCommandFrame.RequestType.EMPTY)},
    };
  }

  @Test(dataProvider = "data")
  public void testInterceptor(@Nonnull byte[] bytes, @Nullable RsceCommandFrame response) {
    byteBuffer.put(bytes);
    byteBuffer.flip();
    Assert.assertEquals(interceptor.getBaudRate(), new NmisBytesInterceptor().getBaudRate());
    Collection<RsceCommandFrame> frames = interceptor.apply(byteBuffer);
    byteBuffer.clear();

    if (response == null) {
      Assert.assertTrue(frames.isEmpty());
    }
    else {
      Assert.assertEquals(frames, Collections.singleton(response));
      Assert.assertTrue(interceptor.putOut(NmisRequest.Sequence.ROTATE_INV.build()).remaining() > 0);
    }
  }
}