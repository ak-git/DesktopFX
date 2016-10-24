package com.ak.hardware.nmisr.comm.interceptor;

import java.nio.ByteBuffer;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.comm.interceptor.AbstractBytesInterceptor;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.hardware.nmis.comm.interceptor.NmisBytesInterceptor;
import com.ak.hardware.nmis.comm.interceptor.NmisProtocolByte;
import com.ak.hardware.nmis.comm.interceptor.NmisRequest;
import com.ak.hardware.nmis.comm.interceptor.NmisResponseFrame;
import com.ak.hardware.rsce.comm.interceptor.RsceBytesInterceptor;
import com.ak.hardware.rsce.comm.interceptor.RsceCommandFrame;

/**
 * RSC Energia Hand Control format wrapped by Neuro-Muscular Test Stand format.
 * <pre>
 *   0х7Е, 0х45 (address for wrapped frame type), Len, CounterLow, CounterHi, DATA_WRAPPED_RSC_Energia ..., CRC
 * </pre>
 * Examples:
 * <pre>
 *   NmisResponseFrame[ 0x7e 0x45 0x02 <b>0x80 0x00</b> 0x45 ] DATA
 *   NmisResponseFrame[ 0x7e 0x45 0x09 <b>0x85 0x00</b> 0x01 0x05 0x0b 0xe0 0xb1 0xe1 0x7a 0x4e ] DATA
 * </pre>
 * each 5 ms.
 */
public final class NmisRsceBytesInterceptor extends AbstractBytesInterceptor<RsceCommandFrame, NmisRequest> {
  private final BytesInterceptor<NmisResponseFrame, NmisRequest> nmis = new NmisBytesInterceptor();
  private final BytesInterceptor<RsceCommandFrame, RsceCommandFrame> rsce = new RsceBytesInterceptor();

  public NmisRsceBytesInterceptor() {
    super("NMISR", NmisProtocolByte.MAX_CAPACITY, NmisRequest.Sequence.CATCH_100.build());
    ByteBuffer buffer = ByteBuffer.allocate(NmisProtocolByte.MAX_CAPACITY);
    nmis.getBufferObservable().subscribe(nmisResponseFrame -> {
      nmisResponseFrame.extractData(buffer);
      buffer.flip();
      if (rsce.write(buffer) == 0) {
        bufferPublish().onNext(RsceCommandFrame.simple(RsceCommandFrame.Control.ALL, RsceCommandFrame.RequestType.EMPTY));
      }
      buffer.clear();
    });
    rsce.getBufferObservable().subscribe(bufferPublish());
  }

  @Nonnegative
  @Override
  public int getBaudRate() {
    return nmis.getBaudRate();
  }

  @Nonnegative
  @Override
  public int write(@Nonnull ByteBuffer src) {
    super.write(src);
    return nmis.write(src);
  }

  @Override
  public void close() {
    rsce.close();
    nmis.close();
    super.close();
  }

  @Override
  protected void innerPut(@Nonnull ByteBuffer outBuffer, @Nonnull NmisRequest request) {
    request.writeTo(outBuffer);
  }
}
