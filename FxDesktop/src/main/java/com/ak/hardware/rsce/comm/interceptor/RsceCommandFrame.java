package com.ak.hardware.rsce.comm.interceptor;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.Checksum;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.ak.comm.interceptor.AbstractBufferFrame;
import com.ak.comm.interceptor.AbstractCheckedBuilder;
import com.ak.comm.interceptor.BytesChecker;

public final class RsceCommandFrame extends AbstractBufferFrame {
  private enum ProtocolByte implements BytesChecker {
    ADDR {
      @Override
      public boolean is(byte b) {
        return b >= Control.ALL.addr && b <= Control.ROTATE.addr;
      }
    },
    LEN {
      @Override
      public boolean is(byte b) {
        return b >= 3 && b <= MAX_CAPACITY - NON_LEN_BYTES;
      }

      @Override
      public void bufferLimit(@Nonnull ByteBuffer buffer) {
        buffer.limit(buffer.get(ordinal()) + NON_LEN_BYTES);
      }
    },
    TYPE {
      @Override
      public boolean is(byte b) {
        return ActionType.find(b) != null && RequestType.find(b) != null;
      }
    }
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

    @Nonnull
    private static Control find(@Nonnull ByteBuffer buffer) {
      return RsceCommandFrame.find(Control.class, buffer, control -> control.addr == buffer.get(ProtocolByte.ADDR.ordinal()));
    }
  }

  enum ActionType {
    NONE, PRECISE, HARD, POSITION, OFF;

    @Nullable
    private static ActionType find(byte b) {
      int n = (b & 0b00_111_000) >> 3;
      return n > -1 && n < values().length ? values()[n] : null;
    }

    @Nonnull
    private static ActionType find(@Nonnull ByteBuffer buffer) {
      return RsceCommandFrame.find(ActionType.class, buffer, actionType -> actionType == find(buffer.get(ProtocolByte.TYPE.ordinal())));
    }
  }

  enum RequestType {
    EMPTY(0), STATUS_I(1), STATUS_I_SPEED(2), STATUS_I_ANGLE(3), STATUS_I_SPEED_ANGLE(4), RESERVE(7);

    private final byte code;

    RequestType(@Nonnegative int code) {
      this.code = (byte) code;
    }

    @Nullable
    private static RequestType find(byte b) {
      return Stream.of(RequestType.values()).filter(type -> type.code == (byte) (b & 0b00000_111)).findAny().orElse(null);
    }

    @Nonnull
    private static RequestType find(@Nonnull ByteBuffer buffer) {
      return RsceCommandFrame.find(RequestType.class, buffer, requestType -> requestType == find(buffer.get(ProtocolByte.TYPE.ordinal())));
    }
  }

  private static final int MAX_CAPACITY = 12;
  private static final int NON_LEN_BYTES = 2;
  private static final Map<String, RsceCommandFrame> SERVOMOTOR_REQUEST_MAP = new ConcurrentHashMap<>();

  private RsceCommandFrame(@Nonnull AbstractCheckedBuilder<RsceCommandFrame> builder) {
    super(builder.buffer());
  }

  @Override
  public String toString() {
    return String.format("%s %s %s %s",
        super.toString(), Control.find(byteBuffer()), ActionType.find(byteBuffer()), RequestType.find(byteBuffer()));
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
    return new RequestBuilder(control, ActionType.POSITION, RequestType.EMPTY).addParam(position).build();
  }

  @Nonnull
  static RsceCommandFrame precise(@Nonnull Control control, @Nonnull RequestType requestType) {
    return precise(control, requestType, control.speed);
  }

  @Nonnull
  static RsceCommandFrame precise(@Nonnull Control control, @Nonnull RequestType requestType, short speed) {
    return new RequestBuilder(control, ActionType.PRECISE, requestType).addParam(speed).build();
  }

  @Nonnull
  private static <E extends Enum<E>> E find(@Nonnull Class<E> clazz, @Nonnull ByteBuffer buffer,
                                            @Nonnull Predicate<? super E> predicate) {
    return StreamSupport.stream(EnumSet.allOf(clazz).spliterator(), true).filter(predicate).findAny().
        orElseThrow(() -> new IllegalArgumentException(Arrays.toString(buffer.array())));
  }

  static class RequestBuilder extends AbstractCheckedBuilder<RsceCommandFrame> {
    @Nonnegative
    private byte codeLength;

    RequestBuilder(@Nonnull Control control, @Nonnull ActionType actionType, @Nonnull RequestType requestType) {
      super(ByteBuffer.allocate(MAX_CAPACITY).order(ByteOrder.LITTLE_ENDIAN));
      codeLength = 3;
      buffer().put(control.addr).put(codeLength).put((byte) ((actionType.ordinal() << 3) + requestType.code));
    }

    RequestBuilder addParam(byte value) {
      buffer().put(value);
      codeLength++;
      return this;
    }

    RequestBuilder addParam(short value) {
      buffer().putShort(value);
      codeLength += 2;
      return this;
    }

    @Nonnull
    @Override
    public RsceCommandFrame build() {
      buffer().put(1, codeLength);
      Checksum checksum = new CRC16IBMChecksum();
      checksum.update(buffer().array(), 0, codeLength);
      buffer().putShort((short) checksum.getValue());
      buffer().flip();
      return new RsceCommandFrame(this);
    }
  }

  static class ResponseBuilder extends AbstractCheckedBuilder<RsceCommandFrame> {
    ResponseBuilder() {
      this(ByteBuffer.allocate(MAX_CAPACITY));
    }

    ResponseBuilder(@Nonnull ByteBuffer buffer) {
      super(buffer);
    }

    @Override
    public boolean is(byte b) {
      boolean okFlag = true;
      if (buffer().position() <= ProtocolByte.values().length) {
        ProtocolByte protocolByte = ProtocolByte.values()[buffer().position() - 1];
        if (protocolByte.is(b)) {
          protocolByte.bufferLimit(buffer());
        }
        else {
          okFlag = false;
        }
      }
      return okFlag;
    }

    @Nullable
    @Override
    public RsceCommandFrame build() {
      if (buffer().position() == 0) {
        for (ProtocolByte protocolByte : ProtocolByte.values()) {
          if (!protocolByte.is(buffer().get())) {
            logWarning(null);
            return null;
          }
        }
      }

      int codeLength = buffer().limit() - NON_LEN_BYTES;
      Checksum checksum = new CRC16IBMChecksum();
      checksum.update(buffer().array(), 0, codeLength);
      if (buffer().order(ByteOrder.LITTLE_ENDIAN).getShort(codeLength) == (short) checksum.getValue()) {
        return new RsceCommandFrame(this);
      }
      else {
        logWarning(null);
        return null;
      }
    }
  }

  @Nonnull
  private static RsceCommandFrame getInstance(@Nonnull Control control, @Nonnull ActionType actionType, @Nonnull RequestType requestType) {
    String key = String.format("%s(%s)_%s(%s)_%s(%s)",
        control.getClass().getSimpleName(), control.name(),
        actionType.getClass().getSimpleName(), actionType.name(),
        requestType.getClass().getSimpleName(), requestType.name());
    if (!SERVOMOTOR_REQUEST_MAP.containsKey(key)) {
      SERVOMOTOR_REQUEST_MAP.putIfAbsent(key, new RequestBuilder(control, actionType, requestType).build());
    }
    return SERVOMOTOR_REQUEST_MAP.get(key);
  }
}
