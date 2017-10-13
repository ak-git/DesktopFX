package com.ak.comm.bytes.rsce;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.zip.Checksum;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.ak.comm.bytes.AbstractCheckedBuilder;
import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.bytes.BytesChecker;

public final class RsceCommandFrame extends BufferFrame {
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
        return ActionType.NONE.find(b) != null && RequestType.EMPTY.find(b) != null;
      }
    }
  }

  public enum Control {
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

    private static Control find(@Nonnull ByteBuffer buffer) {
      return RsceCommandFrame.find(Control.class, buffer, control -> control.addr == buffer.get(ProtocolByte.ADDR.ordinal()));
    }
  }

  private interface Findable<E extends Enum<E>> {
    @Nullable
    E find(byte b);
  }

  public enum ActionType implements Findable<ActionType> {
    NONE, PRECISE, HARD, POSITION, OFF;

    @Override
    @Nullable
    public ActionType find(byte b) {
      int n = (b & 0b00_111_000) >> 3;
      return n > -1 && n < values().length ? values()[n] : null;
    }

    private static ActionType find(@Nonnull ByteBuffer buffer) {
      return RsceCommandFrame.find(ActionType.class, buffer);
    }
  }

  public enum RequestType implements Findable<RequestType> {
    EMPTY(0), STATUS_I(1), STATUS_I_SPEED(2), STATUS_I_ANGLE(3), STATUS_I_SPEED_ANGLE(4), RESERVE(7);

    private final byte code;

    RequestType(@Nonnegative int code) {
      this.code = (byte) code;
    }

    @Override
    @Nullable
    public RequestType find(byte b) {
      return Stream.of(RequestType.values()).filter(type -> type.code == (byte) (b & 0b00000_111)).findAny().orElse(null);
    }

    private static RequestType find(@Nonnull ByteBuffer buffer) {
      return RsceCommandFrame.find(RequestType.class, buffer);
    }
  }

  private enum FrameField {
    /**
     * RsceCommandFrame[ 0x00, 0x09, 0xc7, 0x1a, 0x0b, 0xe3, 0x22, 0x10, 0x00, 0x41, 0xe3 ] 11 bytes ALL NONE RESERVE
     * <pre>
     *   0x00 0x09 (Length) 0xc7 (None-Reserve) <b>0xRheo1-Low 0xRheo1-High</b> 0xRheo2-Low 0xRheo2-High 0xInfo-Low 0xInfo-High CRC1 CRC2
     * </pre>
     */
    R1_DOZEN_MILLI_OHM(Control.ALL, ActionType.NONE, RequestType.RESERVE, 3 + 2 + 2 + NON_LEN_BYTES) {
      @Override
      IntStream get(@Nonnull ByteBuffer buffer) {
        return IntStream.of(buffer.getShort(3));
      }
    },
    /**
     * RsceCommandFrame[ 0x00, 0x09, 0xc7, 0x1a, 0x0b, 0xe3, 0x22, 0x10, 0x00, 0x41, 0xe3 ] 11 bytes ALL NONE RESERVE
     * <pre>
     *   0x00 0x09 (Length) 0xc7 (None-Reserve) 0xRheo1-Low 0xRheo1-High <b>0xRheo2-Low 0xRheo2-High</b> 0xInfo-Low 0xInfo-High CRC1 CRC2
     * </pre>
     */
    R2_DOZEN_MILLI_OHM(Control.ALL, ActionType.NONE, RequestType.RESERVE, 3 + 2 + 2 + NON_LEN_BYTES) {
      @Override
      IntStream get(@Nonnull ByteBuffer buffer) {
        return IntStream.of(buffer.getShort(5));
      }
    },
    /**
     * RsceCommandFrame[ 0x00, 0x09, 0xc7, 0x1a, 0x0b, 0xe3, 0x22, 0x10, 0x00, 0x41, 0xe3 ] 11 bytes ALL NONE RESERVE
     * <pre>
     *   0x00 0x09 (Length) 0xc7 (None-Reserve) 0xRheo1-Low 0xRheo1-High 0xRheo2-Low 0xRheo2-High <b>0xInfo-Low 0xInfo-High</b> CRC1 CRC2
     * </pre>
     */
    INFO(Control.ALL, ActionType.NONE, RequestType.RESERVE, 3 + 2 + 2 + 2 + NON_LEN_BYTES) {
      @Override
      IntStream get(@Nonnull ByteBuffer buffer) {
        return IntStream.of(buffer.getShort(7));
      }
    },
    /**
     * RsceCommandFrame[ 0x01, 0x04, 0x18, 0x01, 0x8b, 0xd9 ] 6 bytes CATCH POSITION EMPTY
     * <pre>
     *   0x01 (Catch) 0x04 (Length) 0x18 (Position-Empty) <b>0xCatchValue</b> CRC1 CRC2
     * </pre>
     */
    CATCH(Control.CATCH, ActionType.POSITION, RequestType.EMPTY, 3 + 1 + NON_LEN_BYTES) {
      @Override
      IntStream get(@Nonnull ByteBuffer buffer) {
        return IntStream.of(buffer.get(3));
      }
    },
    /**
     * RsceCommandFrame[ 0x03, 0x04, 0x18, 0x64, 0x4a, 0x4a ] 6 bytes ROTATE POSITION EMPTY
     * <pre>
     *   0x03 (Rotate) 0x04 (Length) 0x18 (Position-Empty) <b>0xRotateValue</b> CRC1 CRC2
     * </pre>
     */
    ROTATE(Control.ROTATE, ActionType.POSITION, RequestType.EMPTY, 3 + 1 + NON_LEN_BYTES) {
      @Override
      IntStream get(@Nonnull ByteBuffer buffer) {
        return CATCH.get(buffer);
      }
    };

    @Nonnull
    private final String typeCode;
    @Nonnegative
    private final int minFrameLength;

    FrameField(@Nonnull Control control, @Nonnull ActionType actionType, @Nonnull RequestType requestType, @Nonnegative int minFrameLength) {
      typeCode = toType(control, actionType, requestType);
      this.minFrameLength = minFrameLength;
    }

    IntStream get(@Nonnull ByteBuffer buffer) {
      return IntStream.empty();
    }

    private IntStream extract(@Nonnull ByteBuffer buffer) {
      if (buffer.limit() >= minFrameLength && typeCode.equals(toType(buffer))) {
        return get(buffer);
      }
      else {
        return IntStream.empty();
      }
    }
  }

  private static final int MAX_CAPACITY = 12;
  private static final int NON_LEN_BYTES = 2;
  private static final Map<String, RsceCommandFrame> SERVOMOTOR_REQUEST_MAP = new ConcurrentHashMap<>();

  private RsceCommandFrame(@Nonnull AbstractCheckedBuilder<RsceCommandFrame> builder) {
    super(builder.buffer());
  }

  public IntStream getRDozenMilliOhms() {
    return Stream.of(FrameField.R1_DOZEN_MILLI_OHM, FrameField.R2_DOZEN_MILLI_OHM).flatMapToInt(f -> f.extract(byteBuffer()));
  }

  public IntStream getInfoOnes() {
    return Stream.of(FrameField.INFO).flatMapToInt(f -> f.extract(byteBuffer()));
  }

  public int getCatchPercent(int orElse) {
    return Stream.of(FrameField.CATCH).flatMapToInt(f -> f.extract(byteBuffer())).findAny().orElse(orElse);
  }

  public int getRotatePercent(int orElse) {
    return Stream.of(FrameField.ROTATE).flatMapToInt(f -> f.extract(byteBuffer())).findAny().orElse(orElse);
  }

  @Override
  public String toString() {
    return String.format("%s %s", super.toString(), toType(byteBuffer()));
  }

  public static RsceCommandFrame simple(@Nonnull Control control, @Nonnull RequestType requestType) {
    return getInstance(control, ActionType.NONE, requestType);
  }

  public static RsceCommandFrame off(@Nonnull Control control) {
    return getInstance(control, ActionType.OFF, RequestType.EMPTY);
  }

  public static RsceCommandFrame position(@Nonnull Control control, byte position) {
    return new RequestBuilder(control, ActionType.POSITION, RequestType.EMPTY).addParam(position).build();
  }

  public static RsceCommandFrame precise(@Nonnull Control control, @Nonnull RequestType requestType) {
    return precise(control, requestType, control.speed);
  }

  public static RsceCommandFrame precise(@Nonnull Control control, @Nonnull RequestType requestType, short speed) {
    return new RequestBuilder(control, ActionType.PRECISE, requestType).addParam(speed).build();
  }

  private static <E extends Enum<E>> E find(@Nonnull Class<E> clazz, @Nonnull ByteBuffer buffer,
                                            @Nonnull Predicate<? super E> predicate) {
    return EnumSet.allOf(clazz).parallelStream().filter(predicate).findAny().
        orElseThrow(() -> new IllegalArgumentException(Arrays.toString(buffer.array())));
  }

  private static <E extends Enum<E> & Findable<E>> E find(@Nonnull Class<E> clazz, @Nonnull ByteBuffer buffer) {
    return find(clazz, buffer, e -> e == e.find(buffer.get(ProtocolByte.TYPE.ordinal())));
  }

  public static class RequestBuilder extends AbstractCheckedBuilder<RsceCommandFrame> {
    @Nonnegative
    private byte codeLength;

    public RequestBuilder(@Nonnull Control control, @Nonnull ActionType actionType, @Nonnull RequestType requestType) {
      super(ByteBuffer.allocate(MAX_CAPACITY).order(ByteOrder.LITTLE_ENDIAN));
      codeLength = 3;
      buffer().put(control.addr).put(codeLength).put((byte) ((actionType.ordinal() << 3) + requestType.code));
    }

    public RequestBuilder addParam(byte value) {
      buffer().put(value);
      codeLength++;
      return this;
    }

    public RequestBuilder addParam(short value) {
      buffer().putShort(value);
      codeLength += 2;
      return this;
    }

    @Override
    public RsceCommandFrame build() {
      buffer().put(1, codeLength);
      buffer().putShort((short) getChecksum(buffer(), codeLength));
      buffer().flip();
      return new RsceCommandFrame(this);
    }
  }

  public static class ResponseBuilder extends AbstractCheckedBuilder<RsceCommandFrame> {
    public ResponseBuilder() {
      this(ByteBuffer.allocate(MAX_CAPACITY));
    }

    public ResponseBuilder(@Nonnull ByteBuffer buffer) {
      super(buffer.order(ByteOrder.LITTLE_ENDIAN));
    }

    @Override
    public boolean is(byte b) {
      boolean okFlag = true;
      if (buffer().position() <= ProtocolByte.values().length) {
        if (!ProtocolByte.values()[buffer().position() - 1].isCheckedAndLimitSet(b, buffer())) {
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
            logWarning();
            return null;
          }
        }
      }

      int codeLength = buffer().limit() - NON_LEN_BYTES;
      if (buffer().getShort(codeLength) == (short) getChecksum(buffer(), codeLength)) {
        return new RsceCommandFrame(this);
      }
      else {
        logWarning();
        return null;
      }
    }
  }

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

  private static long getChecksum(@Nonnull ByteBuffer buffer, @Nonnegative int codeLength) {
    Checksum checksum = new CRC16IBMChecksum();
    checksum.update(buffer.array(), 0, codeLength);
    return checksum.getValue();
  }

  private static String toType(@Nonnull ByteBuffer byteBuffer) {
    return toType(Control.find(byteBuffer), ActionType.find(byteBuffer), RequestType.find(byteBuffer));
  }

  private static String toType(@Nonnull Control control, @Nonnull ActionType actionType, @Nonnull RequestType requestType) {
    return String.format("%s %s %s", control.name(), actionType.name(), requestType.name());
  }
}
