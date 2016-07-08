package com.ak.hardware.tnmi.comm.interceptor;

import java.nio.ByteBuffer;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.ak.comm.interceptor.AbstractBytesInterceptor;

@Immutable
public final class TnmiBytesInterceptor extends AbstractBytesInterceptor<TnmiResponseFrame, TnmiRequest> {
  private final ByteBuffer byteBuffer = ByteBuffer.allocate(TnmiProtocolByte.MAX_CAPACITY);

  public TnmiBytesInterceptor() {
    super("TNMI", TnmiProtocolByte.MAX_CAPACITY, TnmiRequest.Sequence.CATCH_100.build());
  }

  @Override
  public int write(@Nonnull ByteBuffer src) {
    src.rewind();
    int countResponse = 0;
    while (src.hasRemaining()) {
      byte b = src.get();
      boolean skip = false;
      for (TnmiProtocolByte checkedByte : TnmiProtocolByte.CHECKED_BYTES) {
        if (byteBuffer.position() == checkedByte.ordinal()) {
          if (checkedByte.is(b)) {
            if (checkedByte == TnmiProtocolByte.LEN) {
              byteBuffer.limit(b + 4);
            }
          }
          else {
            byteBuffer.clear();
            skip = !TnmiProtocolByte.START.is(b);
          }
          break;
        }
      }

      if (!skip) {
        byteBuffer.put(b);
      }

      if (!byteBuffer.hasRemaining()) {
        byte[] array = new byte[byteBuffer.limit()];
        byteBuffer.rewind();
        byteBuffer.get(array);
        TnmiResponseFrame response = TnmiResponseFrame.newInstance(array);
        if (response != null) {
          bufferPublish().onNext(response);
          countResponse++;
        }
        byteBuffer.clear();
      }
    }
    return countResponse;
  }

  @Override
  protected void innerPut(@Nonnull ByteBuffer outBuffer, @Nonnull TnmiRequest tnmiRequest) {
    tnmiRequest.writeTo(outBuffer);
  }
}
