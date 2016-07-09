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
  }

  private enum ActionType {
    NONE(0), PRECISE(1), HARD(2), OFF(4);

    private final byte code;

    ActionType(@Nonnegative int code) {
      this.code = (byte) code;
    }
  }

  enum RequestType {
    EMPTY(0), STATUS_I(1), STATUS_I_SPEED(2), STATUS_I_ANGLE(3), STATUS_I_SPEED_ANGLE(4);

    private final byte code;

    RequestType(@Nonnegative int code) {
      this.code = (byte) code;
    }
  }

  static final int MAX_CAPACITY = 12;
  private static final int NON_LEN_BYTES = 2;
  private static final Map<String, RsceCommandFrame> SERVOMOTOR_REQUEST_MAP = new ConcurrentHashMap<>();

  private RsceCommandFrame(@Nonnull ByteBuffer buffer) {
    super(ByteBuffer.allocate(buffer.limit()));
    buffer.rewind();
    byteBuffer().put(buffer);
    byteBuffer().flip();
  }

  private RsceCommandFrame(@Nonnull Control control, @Nonnull ActionType actionType, @Nonnull RequestType requestType) {
    this(control, actionType, requestType, ByteBuffer.allocate(0));
  }

  private RsceCommandFrame(@Nonnull Control control, @Nonnull ActionType actionType, @Nonnull RequestType requestType, ByteBuffer parameters) {
    super(ByteBuffer.allocate(NON_LEN_BYTES + 1 + parameters.capacity() + 2).order(ByteOrder.LITTLE_ENDIAN));
    int codeLength = byteBuffer().capacity() - NON_LEN_BYTES;
    byteBuffer().put(control.addr);
    byteBuffer().put((byte) (codeLength));
    byteBuffer().put((byte) ((actionType.code << 3) + requestType.code));
    parameters.rewind();
    byteBuffer().put(parameters);

    Checksum checksum = new CRC16IBMChecksum();
    checksum.update(byteBuffer().array(), 0, codeLength);
    byteBuffer().putShort((short) checksum.getValue());
    byteBuffer().flip();
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
  static RsceCommandFrame precise(@Nonnull Control control, @Nonnull RequestType requestType) {
    return precise(control, requestType, control.speed);
  }

  @Nonnull
  static RsceCommandFrame precise(@Nonnull Control control, @Nonnull RequestType requestType, short speed) {
    return new RsceCommandFrame(control, ActionType.PRECISE, requestType,
        ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(speed));
  }

  @Nullable
  static RsceCommandFrame newInstance(@Nonnull ByteBuffer byteBuffer) {
    int codeLength = byteBuffer.limit() - NON_LEN_BYTES;
    Checksum checksum = new CRC16IBMChecksum();
    checksum.update(byteBuffer.array(), 0, codeLength);
    if (byteBuffer.order(ByteOrder.LITTLE_ENDIAN).getShort(codeLength) == (short) checksum.getValue()) {
      return new RsceCommandFrame(byteBuffer);
    }
    else {
      Logger.getLogger(RsceCommandFrame.class.getName()).log(Level.WARNING,
          String.format("Invalid RSCE response format: {%s}", Arrays.toString(byteBuffer.array())));
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
      SERVOMOTOR_REQUEST_MAP.putIfAbsent(key, new RsceCommandFrame(control, actionType, requestType));
    }
    return SERVOMOTOR_REQUEST_MAP.get(key);
  }
}
