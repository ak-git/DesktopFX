package com.ak.comm.interceptor;

import java.nio.ByteBuffer;

import javax.annotation.Nonnull;

public abstract class AbstractBufferFrame {
  @Nonnull
  private final ByteBuffer byteBuffer;

  protected AbstractBufferFrame(@Nonnull ByteBuffer byteBuffer) {
    this.byteBuffer = byteBuffer;
  }

  @Override
  public String toString() {
    return toString(getClass(), byteBuffer.array());
  }

  @Nonnull
  public static String toString(@Nonnull Class<?> clazz, @Nonnull byte[] bytes) {
    StringBuilder sb = new StringBuilder(clazz.getSimpleName()).append("[ ");
    for (int i : bytes) {
      sb.append(String.format("%#04x ", (i & 0xFF)));
    }
    sb.append("]");
    return sb.toString();
  }

  public final void writeTo(@Nonnull ByteBuffer outBuffer) {
    byteBuffer.rewind();
    outBuffer.put(byteBuffer);
  }

  @Nonnull
  protected final ByteBuffer byteBuffer() {
    return byteBuffer;
  }
}
