package com.ak.comm.bytes;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;

public class BufferFrame {
  private final ByteBuffer byteBuffer;

  protected BufferFrame(ByteBuffer byteBuffer) {
    byteBuffer.rewind();
    this.byteBuffer = ByteBuffer.allocate(byteBuffer.limit()).put(byteBuffer).order(byteBuffer.order());
    this.byteBuffer.flip();
  }

  public BufferFrame(byte[] bytes, ByteOrder byteOrder) {
    byteBuffer = ByteBuffer.wrap(bytes).order(Objects.requireNonNull(byteOrder));
  }

  @Override
  public final boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof BufferFrame that)) {
      return false;
    }
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

  public final void writeTo(ByteBuffer outBuffer) {
    outBuffer.put(byteBuffer);
    byteBuffer.rewind();
  }

  public final int getInt(int index) {
    return byteBuffer.getInt(index);
  }

  public final float getFloat(int index) {
    return byteBuffer.getFloat(index);
  }

  public final int get(int index) {
    return byteBuffer.get(index);
  }

  protected final ByteBuffer byteBuffer() {
    return byteBuffer;
  }
}
