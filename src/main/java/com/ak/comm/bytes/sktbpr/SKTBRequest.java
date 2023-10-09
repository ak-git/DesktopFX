package com.ak.comm.bytes.sktbpr;

import com.ak.comm.bytes.AbstractCheckedBuilder;
import com.ak.comm.bytes.BufferFrame;
import com.ak.util.Builder;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Optional;

public final class SKTBRequest extends BufferFrame {
  public static final SKTBRequest NONE = new SKTBRequest.RequestBuilder(null).build();
  private static final int MAX_ROTATE_VELOCITY = 10;
  private static final int MAX_FLEX_VELOCITY = 3;
  private static final int MAX_GRIP_VELOCITY = 10;
  private static final int MAX_CAPACITY = 11;
  private final byte id;

  private SKTBRequest(@Nonnull ByteBuffer byteBuffer, byte id) {
    super(byteBuffer);
    this.id = id;
  }

  public interface RotateBuilder {
    @Nonnull
    FlexBuilder rotate(int velocity10);
  }

  public interface FlexBuilder {
    @Nonnull
    GripBuilder flex(int velocity3);
  }

  public interface GripBuilder {
    @Nonnull
    Builder<SKTBRequest> grip(int velocity10);
  }

  public static class RequestBuilder extends AbstractCheckedBuilder<SKTBRequest>
      implements RotateBuilder, FlexBuilder, GripBuilder {
    private final byte id;

    private RequestBuilder(@Nullable SKTBRequest prev) {
      super(ByteBuffer.allocate(MAX_CAPACITY).order(ByteOrder.LITTLE_ENDIAN));
      id = Optional.ofNullable(prev).map(sktbRequest -> (byte) (sktbRequest.id + 1)).orElse((byte) 0);
      buffer().put((byte) 0x5a).put(id).put((byte) 8);
    }

    @Nonnull
    public static RotateBuilder of(@Nullable SKTBRequest prev) {
      return new RequestBuilder(prev);
    }

    @Override
    @Nonnull
    public FlexBuilder rotate(int velocity10) {
      return command(velocity10, MAX_ROTATE_VELOCITY);
    }

    @Override
    @Nonnull
    public GripBuilder flex(int velocity3) {
      return command(velocity3, MAX_FLEX_VELOCITY);
    }

    @Override
    @Nonnull
    public Builder<SKTBRequest> grip(int velocity10) {
      return command(velocity10, MAX_GRIP_VELOCITY);
    }

    @Override
    @Nonnull
    public SKTBRequest build() {
      buffer().position(MAX_CAPACITY - 2).putShort((short) 500);
      return new SKTBRequest(buffer(), id);
    }

    @Nonnull
    private RequestBuilder command(int v, @Nonnegative int max) {
      buffer().putShort((short) (Math.min(Math.abs(v), max) * Math.signum(v) * 1000));
      return this;
    }
  }
}
