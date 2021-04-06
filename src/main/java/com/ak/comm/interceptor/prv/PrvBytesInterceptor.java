package com.ak.comm.interceptor.prv;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.LinkedList;

import javax.annotation.Nonnull;
import javax.inject.Named;

import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.interceptor.AbstractBytesInterceptor;
import org.springframework.context.annotation.Profile;

@Named
@Profile("prv")
public final class PrvBytesInterceptor extends AbstractBytesInterceptor<BufferFrame, BufferFrame> {
  private static final int MAX_LEN = 10;
  private static final byte START = '\n'; // 10
  private static final byte STOP = '\r'; // 13
  private final ByteBuffer byteBuffer = ByteBuffer.allocate(MAX_LEN);

  public PrvBytesInterceptor() {
    super("prv", BaudRate.BR_115200, null, MAX_LEN);
  }

  @Nonnull
  @Override
  protected Collection<BufferFrame> innerProcessIn(@Nonnull ByteBuffer src) {
    Collection<BufferFrame> responses = new LinkedList<>();
    while (src.hasRemaining()) {
      byte in = src.get();
      if (byteBuffer.position() > MAX_LEN - 1) {
        byteBuffer.clear();
      }
      byteBuffer.put(in);
      if (in == STOP) {
        if (byteBuffer.get(0) == START) {
          logSkippedBytes(true);
          byte[] array = new byte[byteBuffer.position()];
          byteBuffer.rewind();
          byteBuffer.get(array);
          responses.add(new BufferFrame(array, byteBuffer.order()));
        }
        byteBuffer.clear();
      }
    }
    return responses;
  }
}
