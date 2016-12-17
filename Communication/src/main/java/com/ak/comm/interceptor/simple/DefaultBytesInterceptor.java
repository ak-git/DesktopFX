package com.ak.comm.interceptor.simple;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.LinkedList;

import javax.annotation.Nonnull;

import com.ak.comm.interceptor.AbstractBytesInterceptor;

public final class DefaultBytesInterceptor extends AbstractBytesInterceptor<Integer, Byte> {
  public DefaultBytesInterceptor(BaudRate baudRate) {
    super(baudRate, (byte) 0);
  }

  @Override
  protected void innerPutOut(@Nonnull ByteBuffer outBuffer, @Nonnull Byte aByte) {
    outBuffer.put(aByte);
  }

  @Override
  protected Collection<Integer> innerProcessIn(@Nonnull ByteBuffer src) {
    Collection<Integer> ints = new LinkedList<>();
    while (src.hasRemaining()) {
      ints.add(Byte.toUnsignedInt(src.get()));
    }
    return ints;
  }
}
