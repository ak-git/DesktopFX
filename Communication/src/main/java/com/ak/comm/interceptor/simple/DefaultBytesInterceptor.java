package com.ak.comm.interceptor.simple;

import java.nio.ByteBuffer;
import java.util.Iterator;

import javax.annotation.Nonnull;

import com.ak.comm.interceptor.AbstractBytesInterceptor;
import io.reactivex.Flowable;

public final class DefaultBytesInterceptor extends AbstractBytesInterceptor<Integer, Byte> {
  DefaultBytesInterceptor() {
    super("None", 1, (byte) 0);
  }

  @Override
  protected void innerPutOut(@Nonnull ByteBuffer outBuffer, @Nonnull Byte aByte) {
    outBuffer.put(aByte);
  }

  @Override
  protected Flowable<Integer> innerProcessIn(@Nonnull ByteBuffer src) {
    src.rewind();
    return Flowable.fromIterable(() -> new Iterator<Integer>() {
      @Override
      public boolean hasNext() {
        return src.hasRemaining();
      }

      @Override
      public Integer next() {
        return Byte.toUnsignedInt(src.get());
      }
    });
  }
}
