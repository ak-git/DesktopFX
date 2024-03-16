package com.ak.appliance.nmisr.comm.interceptor;

import com.ak.appliance.nmis.comm.bytes.NmisProtocolByte;
import com.ak.appliance.nmis.comm.bytes.NmisRequest;
import com.ak.appliance.nmis.comm.bytes.NmisResponseFrame;
import com.ak.appliance.nmis.comm.interceptor.NmisBytesInterceptor;
import com.ak.appliance.rsce.comm.bytes.RsceCommandFrame;
import com.ak.appliance.rsce.comm.interceptor.RsceBytesInterceptor;
import com.ak.comm.interceptor.BytesInterceptor;

import java.nio.ByteBuffer;
import java.util.function.Function;
import java.util.stream.Stream;

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
public final class NmisRsceBytesInterceptor implements BytesInterceptor<NmisRequest, RsceCommandFrame> {
  private final BytesInterceptor<NmisRequest, NmisResponseFrame> nmis = new NmisBytesInterceptor();
  private final Function<ByteBuffer, Stream<RsceCommandFrame>> rsce = new RsceBytesInterceptor();
  private final ByteBuffer buffer = ByteBuffer.allocate(NmisProtocolByte.MAX_CAPACITY);

  @Override
  public String name() {
    return "NMIS-RSC Energia";
  }

  @Override
  public int getBaudRate() {
    return nmis.getBaudRate();
  }

  @Override
  public Stream<RsceCommandFrame> apply(ByteBuffer src) {
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
    return NmisRequest.Sequence.CATCH_100.build();
  }

  @Override
  public ByteBuffer putOut(NmisRequest nmisRequest) {
    return nmis.putOut(nmisRequest);
  }
}
