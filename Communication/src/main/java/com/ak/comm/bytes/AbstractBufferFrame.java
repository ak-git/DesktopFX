package com.ak.comm.bytes;

import java.nio.ByteBuffer;

import javax.annotation.Nonnull;

import com.ak.util.Strings;

public abstract class AbstractBufferFrame {
  @Nonnull
  private final ByteBuffer byteBuffer;

  protected AbstractBufferFrame(@Nonnull ByteBuffer byteBuffer) {
    byteBuffer.rewind();
    this.byteBuffer = ByteBuffer.allocate(byteBuffer.limit()).put(byteBuffer).order(byteBuffer.order());
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
    return toString(getClass(), byteBuffer);
  }

  @Nonnull
  public static String toString(@Nonnull Class<?> clazz, @Nonnull ByteBuffer buffer) {
    buffer.rewind();
    StringBuilder sb = new StringBuilder(clazz.getSimpleName()).append("[ ");
    while (buffer.hasRemaining()) {
      sb.append(String.format("%#04x ", (buffer.get() & 0xFF)));
    }
    sb.append("]");
    if (buffer.limit() > 1) {
      sb.append(Strings.SPACE).append(buffer.limit()).append(" bytes");
    }
    buffer.rewind();
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
