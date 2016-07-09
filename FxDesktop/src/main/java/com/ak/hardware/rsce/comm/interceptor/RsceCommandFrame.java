package com.ak.hardware.rsce.comm.interceptor;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.Checksum;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.ak.comm.interceptor.AbstractBufferFrame;
import com.ak.comm.interceptor.BytesChecker;

final class RsceCommandFrame extends AbstractBufferFrame {
  enum ProtocolByte implements BytesChecker {
    ADDR {
      @Override
      public boolean is(byte b) {
        for (Control control : Control.values()) {
          if (control.addr == b) {
            return true;
          }
        }
        return false;
      }
    },
    LEN {
      @Override
      public boolean is(byte b) {
        return b >= 3 && b <= MAX_CAPACITY - NON_LEN_BYTES;
      }

      @Override
      public void buffer(byte b, @Nonnull ByteBuffer buffer) {
        buffer.limit(b + NON_LEN_BYTES);
      }
    };
  }

  enum Control {
    ALL(0x00, 0),
    CATCH(0x01, -20000),
    FINGER(0x02, 10000),
    ROTATE(0x03, 20000);

    private final byte addr;
    private final short speed;

    Control(@Nonnegative int addr, int speed) {
      this.addr = (byte) addr;
      this.speed = (short) speed;
    }

    static Control find(ByteBuffer buffer) {
      for (Control control : values()) {
        if (buffer.get(0) == control.addr) {
          return control;
        }
      }
      throw new IllegalArgumentException(Arrays.toString(buffer.array()));
    }
  }

  enum ActionType {
    NONE, PRECISE, HARD, POSITION, OFF;

    static ActionType find(ByteBuffer buffer) {
      for (ActionType actionType : values()) {
        if (((buffer.get(2) & 0b00_111_000) >> 3) == (byte) actionType.ordinal()) {
          return actionType;
        }
      }
      throw new IllegalArgumentException(Arrays.toString(buffer.array()));
    }
  }

  enum RequestType {
    EMPTY(0), STATUS_I(1), STATUS_I_SPEED(2), STATUS_I_ANGLE(3), STATUS_I_SPEED_ANGLE(4), RESERVE(7);

    private final byte code;

    RequestType(@Nonnegative int code) {
      this.code = (byte) code;
    }

    static RequestType find(ByteBuffer buffer) {
      for (RequestType requestType : values()) {
        if ((buffer.get(2) & 0b0000_0111) == requestType.code) {
          return requestType;
        }
      }
      throw new IllegalArgumentException(Arrays.toString(buffer.array()));
    }
  }

  static final int MAX_CAPACITY = 12;
  private static final int NON_LEN_BYTES = 2;
  private static final Map<String, RsceCommandFrame> SERVOMOTOR_REQUEST_MAP = new ConcurrentHashMap<>();
  @Nonnull
  private final String toString;

  private RsceCommandFrame(@Nonnull ByteBuffer buffer) {
    super(ByteBuffer.allocate(buffer.limit()));
    toString = String.format("%s %s %s", Control.find(buffer), ActionType.find(buffer), RequestType.find(buffer));
    buffer.rewind();
    byteBuffer().put(buffer);
    byteBuffer().flip();
  }

  @Nonnull
  @Override
  public String toString() {
    return String.format("%s %s", AbstractBufferFrame.toString(getClass(), byteBuffer().array()), toString);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof RsceCommandFrame)) {
      return false;
    }

    RsceCommandFrame that = (RsceCommandFrame) o;
    return byteBuffer().equals(that.byteBuffer());
  }

  @Override
  public int hashCode() {
    return byteBuffer().hashCode();
  }

  @Nonnull
  static RsceCommandFrame simple(@Nonnull Control control, @Nonnull RequestType requestType) {
    return getInstance(control, ActionType.NONE, requestType);
  }

  @Nonnull
  static RsceCommandFrame off(@Nonnull Control control) {
    return getInstance(control, ActionType.OFF, RequestType.EMPTY);
  }

  @Nonnull
  static RsceCommandFrame position(@Nonnull Control control, byte position) {
    return new RsceCommandFrame.Builder(control, ActionType.POSITION, RequestType.EMPTY).addParam(position).build();
  }

  @Nonnull
  static RsceCommandFrame precise(@Nonnull Control control, @Nonnull RequestType requestType) {
    return precise(control, requestType, control.speed);
  }

  @Nonnull
  static RsceCommandFrame precise(@Nonnull Control control, @Nonnull RequestType requestType, short speed) {
    return new RsceCommandFrame.Builder(control, ActionType.PRECISE, requestType).addParam(speed).build();
  }

  static class Builder implements javafx.util.Builder<RsceCommandFrame> {
    private final ByteBuffer byteBuffer = ByteBuffer.allocate(MAX_CAPACITY).order(ByteOrder.LITTLE_ENDIAN);
    private byte codeLength;

    Builder(@Nonnull Control control, @Nonnull ActionType actionType, @Nonnull RequestType requestType) {
      codeLength = 3;
      byteBuffer.put(control.addr);
      byteBuffer.put(codeLength);
      byteBuffer.put((byte) ((actionType.ordinal() << 3) + requestType.code));
    }

    Builder addParam(byte value) {
      byteBuffer.put(value);
      codeLength++;
      return this;
    }

    Builder addParam(short value) {
      byteBuffer.putShort(value);
      codeLength += 2;
      return this;
    }

    @Nonnull
    @Override
    public RsceCommandFrame build() {
      byteBuffer.put(1, codeLength);
      Checksum checksum = new CRC16IBMChecksum();
      checksum.update(byteBuffer.array(), 0, codeLength);
      byteBuffer.putShort((short) checksum.getValue());
      byteBuffer.flip();
      return new RsceCommandFrame(byteBuffer);
    }
  }

  @Nullable
  static RsceCommandFrame newInstance(@Nonnull ByteBuffer byteBuffer) {
    int codeLength = byteBuffer.limit() - NON_LEN_BYTES;
    Checksum checksum = new CRC16IBMChecksum();
    checksum.update(byteBuffer.array(), 0, codeLength);
    if (byteBuffer.order(ByteOrder.LITTLE_ENDIAN).getShort(codeLength) == (short) checksum.getValue()) {
      try {
        return new RsceCommandFrame(byteBuffer);
      }
      catch (Exception e) {
        logWarning(byteBuffer, e);
        return null;
      }
    }
    else {
      logWarning(byteBuffer, null);
      return null;
    }
  }

  @Nonnull
  private static RsceCommandFrame getInstance(@Nonnull Control control, @Nonnull ActionType actionType, @Nonnull RequestType requestType) {
    String key = String.format("%s(%s)_%s(%s)_%s(%s)",
        control.getClass().getSimpleName(), control.name(),
        actionType.getClass().getSimpleName(), actionType.name(),
        requestType.getClass().getSimpleName(), requestType.name());
    if (!SERVOMOTOR_REQUEST_MAP.containsKey(key)) {
      SERVOMOTOR_REQUEST_MAP.putIfAbsent(key, new RsceCommandFrame.Builder(control, actionType, requestType).build());
    }
    return SERVOMOTOR_REQUEST_MAP.get(key);
  }

  private static void logWarning(@Nonnull ByteBuffer byteBuffer, Exception e) {
    Logger.getLogger(RsceCommandFrame.class.getName()).log(Level.CONFIG,
        String.format("Invalid RSCE response format: {%s}", Arrays.toString(byteBuffer.array())), e);
  }
}
