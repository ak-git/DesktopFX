package com.ak.comm.interceptor.nmisr;

import java.nio.ByteBuffer;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.comm.bytes.nmis.NmisProtocolByte;
import com.ak.comm.bytes.nmis.NmisRequest;
import com.ak.comm.bytes.nmis.NmisResponseFrame;
import com.ak.comm.bytes.rsce.RsceCommandFrame;
import com.ak.comm.interceptor.AbstractBytesInterceptor;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.comm.interceptor.nmis.NmisBytesInterceptor;
import com.ak.comm.interceptor.rsce.RsceBytesInterceptor;
import io.reactivex.Flowable;
import org.reactivestreams.Publisher;

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
final class NmisRsceBytesInterceptor extends AbstractBytesInterceptor<RsceCommandFrame, NmisRequest> {
  private final BytesInterceptor<NmisResponseFrame, NmisRequest> nmis = new NmisBytesInterceptor();
  private final BytesInterceptor<RsceCommandFrame, RsceCommandFrame> rsce = new RsceBytesInterceptor();
  private final ByteBuffer buffer = ByteBuffer.allocate(NmisProtocolByte.MAX_CAPACITY);

  NmisRsceBytesInterceptor() {
    super("NMISR", NmisProtocolByte.MAX_CAPACITY, NmisRequest.Sequence.CATCH_100.build());
  }

  @Nonnegative
  @Override
  public int getBaudRate() {
    return nmis.getBaudRate();
  }

  @Override
  protected void innerPutOut(@Nonnull ByteBuffer outBuffer, @Nonnull NmisRequest nmisRequest) {
    nmisRequest.writeTo(outBuffer);
  }

  @Override
  protected Flowable<RsceCommandFrame> innerProcessIn(@Nonnull ByteBuffer src) {
    return Flowable.fromPublisher(nmis.apply(src)).flatMap(nmisResponseFrame -> {
      nmisResponseFrame.extractData(buffer);
      buffer.flip();
      Publisher<RsceCommandFrame> publisher = rsce.apply(buffer);
      buffer.clear();
      return publisher;
    });
  }
}
