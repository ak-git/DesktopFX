package com.ak.comm.bytes;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.util.LogUtils;

public class BufferFrame {
  @Nonnull
  private final ByteBuffer byteBuffer;

  protected BufferFrame(@Nonnull ByteBuffer byteBuffer) {
    byteBuffer.rewind();
    this.byteBuffer = ByteBuffer.allocate(byteBuffer.limit()).put(byteBuffer).order(byteBuffer.order());
    this.byteBuffer.flip();
  }

  public BufferFrame(@Nonnull byte[] bytes, @Nonnull ByteOrder byteOrder) {
    byteBuffer = ByteBuffer.wrap(bytes).order(byteOrder);
  }

  @Override
  public final boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof BufferFrame)) {
      return false;
    }

    BufferFrame that = (BufferFrame) o;
    return byteBuffer.equals(that.byteBuffer);
  }

  @Override
  public final int hashCode() {
    return byteBuffer.hashCode();
  }

  @Override
  public String toString() {
    return LogUtils.toString(getClass(), byteBuffer);
  }

  public final void writeTo(@Nonnull ByteBuffer outBuffer) {
    outBuffer.put(byteBuffer);
    byteBuffer.rewind();
  }

  public final int getInt(@Nonnegative int index) {
    return byteBuffer.getInt(index);
  }

  public final float getFloat(@Nonnegative int index) {
    return byteBuffer.getFloat(index);
  }

  public final int get(@Nonnegative int index) {
    return byteBuffer.get(index);
  }

  protected final ByteBuffer byteBuffer() {
    return byteBuffer;
  }
}
