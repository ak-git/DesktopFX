package com.ak.comm.bytes;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.annotation.Nonnull;

import static com.ak.util.Strings.SPACE;

public class BufferFrame {
  @Nonnull
  private final ByteBuffer byteBuffer;

  protected BufferFrame(@Nonnull ByteBuffer byteBuffer) {
    byteBuffer.rewind();
    this.byteBuffer = ByteBuffer.allocate(byteBuffer.limit()).put(byteBuffer).order(byteBuffer.order());
    this.byteBuffer.flip();
  }

  public BufferFrame(@Nonnull byte[] bytes, ByteOrder byteOrder) {
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
    return toString(getClass(), byteBuffer);
  }

  public static String toString(@Nonnull Class<?> clazz, @Nonnull ByteBuffer buffer) {
    buffer.rewind();
    StringBuilder sb = new StringBuilder(clazz.getSimpleName()).append("[ ");
    while (buffer.hasRemaining()) {
      sb.append(String.format("%#04x", (buffer.get() & 0xFF)));
      if (buffer.hasRemaining()) {
        sb.append(',');
      }
      sb.append(SPACE);
    }
    sb.append("]");
    if (buffer.limit() > 1) {
      sb.append(SPACE).append(buffer.limit()).append(" bytes");
    }
    buffer.rewind();
    return sb.toString();
  }

  public final void writeTo(@Nonnull ByteBuffer outBuffer) {
    outBuffer.put(byteBuffer);
    byteBuffer.rewind();
  }

  public final int getInt(int index) {
    return byteBuffer.getInt(index);
  }

  protected final ByteBuffer byteBuffer() {
    return byteBuffer;
  }

  @Override
  public final Object clone() throws CloneNotSupportedException {
    throw new CloneNotSupportedException();
  }
}
