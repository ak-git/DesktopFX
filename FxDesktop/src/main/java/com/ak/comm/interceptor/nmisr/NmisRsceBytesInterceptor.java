package com.ak.comm.interceptor.nmisr;

import java.nio.ByteBuffer;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import com.ak.comm.bytes.nmis.NmisProtocolByte;
import com.ak.comm.bytes.nmis.NmisRequest;
import com.ak.comm.bytes.nmis.NmisResponseFrame;
import com.ak.comm.bytes.rsce.RsceCommandFrame;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.comm.interceptor.nmis.NmisBytesInterceptor;
import com.ak.comm.interceptor.rsce.RsceBytesInterceptor;

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
public final class NmisRsceBytesInterceptor implements BytesInterceptor<RsceCommandFrame, NmisRequest> {
  private static final NmisRequest.Sequence[] PINGS = {
      NmisRequest.Sequence.CATCH_100, NmisRequest.Sequence.CATCH_60, NmisRequest.Sequence.CATCH_30,
      NmisRequest.Sequence.ROTATE_100, NmisRequest.Sequence.ROTATE_60, NmisRequest.Sequence.ROTATE_30};
  private final BytesInterceptor<NmisResponseFrame, NmisRequest> nmis = new NmisBytesInterceptor();
  private final Function<ByteBuffer, Stream<RsceCommandFrame>> rsce = new RsceBytesInterceptor();
  private final ByteBuffer buffer = ByteBuffer.allocate(NmisProtocolByte.MAX_CAPACITY);
  private int pingIndex = -1;

  @Override
  public int getBaudRate() {
    return nmis.getBaudRate();
  }

  @Override
  public Stream<RsceCommandFrame> apply(@Nonnull ByteBuffer src) {
    return nmis.apply(src).flatMap(nmisResponseFrame -> {
      buffer.clear();
      nmisResponseFrame.extractData(buffer);
      buffer.flip();
      if (buffer.hasRemaining()) {
        return rsce.apply(buffer);
      }
      else {
        return Stream.of(RsceCommandFrame.simple(RsceCommandFrame.Control.ALL, RsceCommandFrame.RequestType.EMPTY));
      }
    });
  }

  @Override
  public NmisRequest getPingRequest() {
    return PINGS[(++pingIndex) % PINGS.length].build();
  }

  @Override
  public ByteBuffer putOut(@Nonnull NmisRequest nmisRequest) {
    return nmis.putOut(nmisRequest);
  }
}
