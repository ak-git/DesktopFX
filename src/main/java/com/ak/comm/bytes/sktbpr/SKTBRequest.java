package com.ak.comm.bytes.sktbpr;

import com.ak.comm.bytes.AbstractCheckedBuilder;
import com.ak.comm.bytes.BufferFrame;
import com.ak.util.Builder;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class SKTBRequest extends BufferFrame {
  public static final SKTBRequest NONE = new RequestBuilder((byte) 0).build();
  private static final int MAX_ROTATE_VELOCITY = 10_000;
  private static final int MAX_FLEX_VELOCITY = 3_000;
  private static final int MAX_GRIP_VELOCITY = 10_000;
  private static final int MAX_CAPACITY = 11;
  private final byte id;

  private SKTBRequest(@Nonnull ByteBuffer byteBuffer, byte id) {
    super(byteBuffer);
    this.id = id;
  }

  @Nonnull
  public RotateBuilder from() {
    return new RequestBuilder((byte) (id + 1));
  }

  public interface RotateBuilder {
    @Nonnull
    FlexBuilder rotate(int velocity);
  }

  public interface FlexBuilder {
    @Nonnull
    GripBuilder flex(int velocity);
  }

  public interface GripBuilder {
    @Nonnull
    Builder<SKTBRequest> grip(int velocity);
  }

  public static class RequestBuilder extends AbstractCheckedBuilder<SKTBRequest>
      implements RotateBuilder, FlexBuilder, GripBuilder {
    private final byte id;

    private RequestBuilder(byte id) {
      super(ByteBuffer.allocate(MAX_CAPACITY).order(ByteOrder.LITTLE_ENDIAN));
      this.id = id;
      buffer().put((byte) 0x5a).put(this.id).put((byte) 8);
    }

    @Override
    @Nonnull
    public FlexBuilder rotate(int velocity) {
      return command(velocity, MAX_ROTATE_VELOCITY);
    }

    @Override
    @Nonnull
    public GripBuilder flex(int velocity) {
      return command(velocity, MAX_FLEX_VELOCITY);
    }

    @Override
    @Nonnull
    public Builder<SKTBRequest> grip(int velocity) {
      return command(velocity, MAX_GRIP_VELOCITY);
    }

    @Override
    @Nonnull
    public SKTBRequest build() {
      buffer().position(MAX_CAPACITY - 2).putShort((short) 500);
      return new SKTBRequest(buffer(), id);
    }

    @Nonnull
    private RequestBuilder command(int v, @Nonnegative int max) {
      buffer().putShort((short) (Math.min(Math.abs(v), max) * Math.signum(v)));
      return this;
    }
  }
}
