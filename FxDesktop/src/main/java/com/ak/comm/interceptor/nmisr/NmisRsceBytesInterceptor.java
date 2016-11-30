package com.ak.comm.interceptor.nmisr;

import java.nio.ByteBuffer;

import javax.annotation.Nonnull;

import com.ak.comm.bytes.nmis.NmisProtocolByte;
import com.ak.comm.bytes.nmis.NmisRequest;
import com.ak.comm.bytes.nmis.NmisResponseFrame;
import com.ak.comm.bytes.rsce.RsceCommandFrame;
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
final class NmisRsceBytesInterceptor implements BytesInterceptor<RsceCommandFrame, NmisRequest> {
  private final BytesInterceptor<NmisResponseFrame, NmisRequest> nmis = new NmisBytesInterceptor();
  private final BytesInterceptor<RsceCommandFrame, RsceCommandFrame> rsce = new RsceBytesInterceptor();
  private final ByteBuffer buffer = ByteBuffer.allocate(NmisProtocolByte.MAX_CAPACITY);

  @Override
  public String name() {
    return "NMISR";
  }

  @Override
  public int getBaudRate() {
    return nmis.getBaudRate();
  }

  @Override
  public Publisher<RsceCommandFrame> apply(@Nonnull ByteBuffer src) {
    return Flowable.fromPublisher(nmis.apply(src)).flatMap(nmisResponseFrame -> {
      buffer.clear();
      nmisResponseFrame.extractData(buffer);
      buffer.flip();
      return rsce.apply(buffer);
    });
  }

  @Override
  public NmisRequest getPingRequest() {
    return NmisRequest.Sequence.CATCH_100.build();
  }

  @Override
  public ByteBuffer putOut(@Nonnull NmisRequest nmisRequest) {
    return nmis.putOut(nmisRequest);
  }
}
