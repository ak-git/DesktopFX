package com.ak.appliance.sktbpr.comm.bytes;

import com.ak.comm.bytes.AbstractCheckedBuilder;
import com.ak.comm.bytes.BufferFrame;
import com.ak.util.Builder;

import javax.annotation.Nonnegative;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class SKTBRequest extends BufferFrame {
  public static final SKTBRequest NONE = new RequestBuilder((byte) 0).build();
  private static final int MAX_ROTATE_VELOCITY = 10_000;
  private static final int MAX_FLEX_VELOCITY = 3_000;
  private static final int MAX_GRIP_VELOCITY = 10_000;
  private static final int MAX_CAPACITY = 11;
  private final byte id;

  private SKTBRequest(ByteBuffer byteBuffer, byte id) {
    super(byteBuffer);
    this.id = id;
  }

  public RotateBuilder from() {
    return new RequestBuilder((byte) (id + 1));
  }

  public interface RotateBuilder {
    FlexBuilder rotate(int velocity);
  }

  public interface FlexBuilder {
    GripBuilder flex(int velocity);
  }

  public interface GripBuilder {
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
    public FlexBuilder rotate(int velocity) {
      return command(velocity, MAX_ROTATE_VELOCITY);
    }

    @Override
    public GripBuilder flex(int velocity) {
      return command(velocity, MAX_FLEX_VELOCITY);
    }

    @Override
    public Builder<SKTBRequest> grip(int velocity) {
      return command(velocity, MAX_GRIP_VELOCITY);
    }

    @Override
    public SKTBRequest build() {
      buffer().position(MAX_CAPACITY - 2).putShort((short) 500);
      return new SKTBRequest(buffer(), id);
    }

    private RequestBuilder command(int v, @Nonnegative int max) {
      buffer().putShort((short) (Math.min(Math.abs(v), max) * Math.signum(v)));
      return this;
    }
  }
}
