package com.ak.comm.bytes.sktbpr;

import com.ak.comm.bytes.AbstractCheckedBuilder;
import com.ak.comm.bytes.BufferFrame;

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

  public static class RequestBuilder extends AbstractCheckedBuilder<SKTBRequest> {
    private final byte id;

    public RequestBuilder(@Nullable SKTBRequest prev) {
      super(ByteBuffer.allocate(MAX_CAPACITY).order(ByteOrder.LITTLE_ENDIAN));
      id = Optional.ofNullable(prev).map(sktbRequest -> (byte) (sktbRequest.id + 1)).orElse((byte) 0);
      buffer().put((byte) 0x5a).put(id).put((byte) 8);
    }

    public AbstractCheckedBuilder<SKTBRequest> rotate(int velocity10) {
      return command(velocity10, MAX_ROTATE_VELOCITY).command(0, 0).command(0, 0);
    }

    public AbstractCheckedBuilder<SKTBRequest> flex(int velocity3) {
      return command(0, 0).command(velocity3, MAX_FLEX_VELOCITY).command(0, 0);
    }

    public AbstractCheckedBuilder<SKTBRequest> grip(int velocity10) {
      return command(0, 0).command(0, 0).command(velocity10, MAX_GRIP_VELOCITY);
    }

    @Override
    public SKTBRequest build() {
      buffer().position(MAX_CAPACITY - 2).putShort((short) 500);
      return new SKTBRequest(buffer(), id);
    }

    private RequestBuilder command(int v, @Nonnegative int max) {
      buffer().putShort((short) (Math.min(Math.abs(v), max) * Math.signum(v) * 1000));
      return this;
    }
  }
}
