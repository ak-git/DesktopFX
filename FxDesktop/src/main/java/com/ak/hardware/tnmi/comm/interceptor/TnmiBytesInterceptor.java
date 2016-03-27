package com.ak.hardware.tnmi.comm.interceptor;

import java.nio.ByteBuffer;
import java.util.Arrays;

import com.ak.comm.interceptor.AbstractBytesInterceptor;

public final class TnmiBytesInterceptor extends AbstractBytesInterceptor<TnmiResponse, TnmiRequest> {
  private final ByteBuffer byteBuffer = ByteBuffer.allocate(TnmiProtocolByte.MAX_CAPACITY);

  public TnmiBytesInterceptor() {
    super(TnmiProtocolByte.MAX_CAPACITY, TnmiRequest.Single.Z_360.buildForAll(TnmiRequest.MyoType.OFF, TnmiRequest.MyoFrequency.OFF));
  }

  @Override
  public int write(ByteBuffer src) {
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
        TnmiResponse response = TnmiResponse.newInstance(array);
        if (response == null) {
          bufferPublish().onError(new Exception(String.format("Invalid TNMI response format: {%s}", Arrays.toString(array))));
        }
        else {
          bufferPublish().onNext(response);
          countResponse++;
        }
        byteBuffer.clear();
      }
    }
    return countResponse;
  }

  @Override
  protected void innerPut(ByteBuffer outBuffer, TnmiRequest tnmiRequest) {
    tnmiRequest.writeTo(outBuffer);
  }
}
