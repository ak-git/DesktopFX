package com.ak.comm.interceptor;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class AbstractBufferFrame {
  @Nonnull
  private final ByteBuffer byteBuffer;

  protected AbstractBufferFrame(@Nonnull ByteBuffer byteBuffer) {
    byteBuffer.rewind();
    this.byteBuffer = ByteBuffer.allocate(byteBuffer.limit()).put(byteBuffer);
    this.byteBuffer.flip();
  }

  protected AbstractBufferFrame(@Nonnull byte[] bytes) {
    byteBuffer = ByteBuffer.wrap(bytes);
  }

  @Override
  public final boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof AbstractBufferFrame)) {
      return false;
    }

    AbstractBufferFrame that = (AbstractBufferFrame) o;
    return byteBuffer.equals(that.byteBuffer);
  }

  @Override
  public final int hashCode() {
    return byteBuffer.hashCode();
  }

  @Nonnull
  @Override
  public String toString() {
    return toString(getClass(), byteBuffer.array());
  }

  protected static void logWarning(@Nonnull ByteBuffer byteBuffer, @Nullable Exception e) {
    Logger.getLogger(AbstractBufferFrame.class.getName()).log(Level.CONFIG,
        String.format("Invalid response format: {%s}", Arrays.toString(byteBuffer.array())), e);
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
    outBuffer.put(byteBuffer);
    byteBuffer.rewind();
  }

  @Nonnull
  protected final ByteBuffer byteBuffer() {
    return byteBuffer;
  }

  @Override
  public final Object clone() throws CloneNotSupportedException {
    throw new CloneNotSupportedException();
  }
}
