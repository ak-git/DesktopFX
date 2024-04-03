package com.ak.appliance.rsce.comm.bytes;

import com.ak.comm.bytes.AbstractCheckedBuilder;
import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.bytes.BytesChecker;
import com.ak.util.Strings;

import javax.annotation.Nonnegative;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.zip.Checksum;

public final class RsceCommandFrame extends BufferFrame {
  private static final int MAX_CAPACITY = 12;
  private static final int NON_LEN_BYTES = 2;

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
      public void bufferLimit(ByteBuffer buffer) {
        buffer.limit(buffer.get(ordinal()) + NON_LEN_BYTES);
      }
    },
    TYPE {
      @Override
      public boolean is(byte b) {
        return ActionType.NONE.find(b).isPresent() && RequestType.EMPTY.find(b).isPresent();
      }
    }
  }

  public enum Control {
    ALL(0x00, 0),
    CATCH(0x01, -20000),
    FINGER(0x02, 10000),
    ROTATE(0x03, 20000);

    @Nonnegative
    private final byte addr;
    private final short speed;

    Control(int addr, int speed) {
      this.addr = (byte) addr;
      this.speed = (short) speed;
    }

    private static Control find(ByteBuffer buffer) {
      return RsceCommandFrame.find(Control.class, control -> control.addr == buffer.get(ProtocolByte.ADDR.ordinal()))
          .orElseThrow(() -> new IllegalArgumentException(Arrays.toString(buffer.array())));
    }
  }

  private interface Findable<E extends Enum<E>> {
    Optional<E> find(byte b);
  }

  public enum ActionType implements Findable<ActionType> {
    NONE, PRECISE, HARD, POSITION, OFF;

    @Override
    public Optional<ActionType> find(byte b) {
      int n = (b & 0b00_111_000) >> 3;
      return n < values().length ? Optional.of(values()[n]) : Optional.empty();
    }

    private static ActionType find(ByteBuffer buffer) {
      return RsceCommandFrame.find(ActionType.class, buffer)
          .orElseThrow(() -> new IllegalArgumentException(Arrays.toString(buffer.array())));
    }
  }

  public enum RequestType implements Findable<RequestType> {
    EMPTY(0), STATUS_I(1), STATUS_I_SPEED(2), STATUS_I_ANGLE(3), STATUS_I_SPEED_ANGLE(4), RESERVE(7);

    @Nonnegative
    private final byte code;

    RequestType(int code) {
      this.code = (byte) code;
    }

    @Override
    public Optional<RequestType> find(byte b) {
      return Stream.of(values()).filter(type -> type.code == (byte) (b & 0b00000_111)).findAny();
    }

    private static RequestType find(ByteBuffer buffer) {
      return RsceCommandFrame.find(RequestType.class, buffer)
          .orElseThrow(() -> new IllegalArgumentException(Arrays.toString(buffer.array())));
    }
  }

  public enum FrameField {
    /**
     * RsceCommandFrame[ 0x00, 0x09, 0xc7, 0x1a, 0x0b, 0xe3, 0x22, 0x10, 0x00, 0x41, 0xe3 ] 11 bytes ALL NONE RESERVE
     * <pre>
     *   0x00 0x09 (Length) 0xc7 (None-Reserve) <b>0xRheo1-Low 0xRheo1-High</b> 0xRheo2-Low 0xRheo2-High 0xInfo-Low 0xInfo-High CRC1 CRC2
     * </pre>
     */
    R1_DOZEN_MILLI_OHM(Control.ALL, ActionType.NONE, RequestType.RESERVE, 3 + 2 + 2 + NON_LEN_BYTES) {
      @Override
      int get(ByteBuffer buffer) {
        return buffer.getShort(3);
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
      int get(ByteBuffer buffer) {
        return buffer.getShort(5);
      }
    },
    /**
     * RsceCommandFrame[ 0x00, 0x09, 0xc7, 0x1a, 0x0b, 0xe3, 0x22, 0x10, 0x00, 0x41, 0xe3 ] 11 bytes ALL NONE RESERVE
     * <pre>
     *   0x00 0x09 (Length) 0xc7 (None-Reserve) 0xRheo1-Low 0xRheo1-High 0xRheo2-Low 0xRheo2-High <b>0xInfo-Low 0xInfo-High</b> CRC1 CRC2
     * </pre>
     */
    ACCELEROMETER(Control.ALL, ActionType.NONE, RequestType.RESERVE, 3 + 2 + 2 + 2 + NON_LEN_BYTES) {
      @Override
      int get(ByteBuffer buffer) {
        return buffer.getShort(7);
      }
    },
    /**
     * RsceCommandFrame[ 0x01, 0x04, 0x18, 0x01, 0x8b, 0xd9 ] 6 bytes CATCH POSITION EMPTY
     * <pre>
     *   0x01 (Catch) 0x04 (Length) 0x18 (Position-Empty) <b>0xOpenValue</b> CRC1 CRC2
     * </pre>
     */
    OPEN_PERCENT(Control.CATCH, ActionType.POSITION, RequestType.EMPTY, 3 + 1 + NON_LEN_BYTES) {
      @Override
      int get(ByteBuffer buffer) {
        return buffer.get(3);
      }
    },
    /**
     * RsceCommandFrame[ 0x03, 0x04, 0x18, 0x64, 0x4a, 0x4a ] 6 bytes ROTATE POSITION EMPTY
     * <pre>
     *   0x03 (Rotate) 0x04 (Length) 0x18 (Position-Empty) <b>0xRotateValue</b> CRC1 CRC2
     * </pre>
     */
    ROTATE_PERCENT(Control.ROTATE, ActionType.POSITION, RequestType.EMPTY, 3 + 1 + NON_LEN_BYTES) {
      @Override
      int get(ByteBuffer buffer) {
        return OPEN_PERCENT.get(buffer);
      }
    },
    /**
     * RsceCommandFrame[ 0x02, 0x05, 0x0c, 0x20, 0x4e, 0x04, 0xfb ] 7 bytes FINGER PRECISE STATUS_I_SPEED_ANGLE
     * <pre>
     *   0x02 (Finger) 0x05 (Length) 0x18 (Position-Empty) <b>0xRotateValue</b> CRC1 CRC2
     * </pre>
     */
    FINGER(Control.FINGER, ActionType.PRECISE, RequestType.STATUS_I_SPEED_ANGLE, 3 + 2 + NON_LEN_BYTES) {
      @Override
      int get(ByteBuffer buffer) {
        return buffer.getShort(3);
      }
    };

    private final String typeCode;
    @Nonnegative
    private final int minFrameLength;

    FrameField(Control control, ActionType actionType, RequestType requestType, @Nonnegative int minFrameLength) {
      typeCode = toType(control, actionType, requestType);
      this.minFrameLength = minFrameLength;
    }

    abstract int get(ByteBuffer buffer);

    public int extract(ByteBuffer buffer, int orElse) {
      if (buffer.limit() >= minFrameLength && typeCode.equals(toType(buffer))) {
        return get(buffer);
      }
      else {
        return orElse;
      }
    }
  }

  private RsceCommandFrame(AbstractCheckedBuilder<?> builder) {
    super(builder.buffer());
  }

  public int extract(FrameField frameField, int orElse) {
    return frameField.extract(byteBuffer(), orElse);
  }

  @Override
  public String toString() {
    return String.join(Strings.SPACE, super.toString(), toType(byteBuffer()));
  }

  public static RsceCommandFrame simple(Control control, RequestType requestType) {
    return new RequestBuilder(control, ActionType.NONE, requestType).build();
  }

  public static RsceCommandFrame off(Control control) {
    return new RequestBuilder(control, ActionType.OFF, RequestType.EMPTY).build();
  }

  public static RsceCommandFrame position(Control control, byte position) {
    return new RequestBuilder(control, ActionType.POSITION, RequestType.EMPTY).addParam(position).build();
  }

  public static RsceCommandFrame precise(Control control, RequestType requestType) {
    return precise(control, requestType, control.speed);
  }

  public static RsceCommandFrame precise(Control control, RequestType requestType, short speed) {
    return new RequestBuilder(control, ActionType.PRECISE, requestType).addParam(speed).build();
  }

  private static <E extends Enum<E>> Optional<E> find(Class<E> clazz, Predicate<? super E> predicate) {
    return EnumSet.allOf(clazz).stream().filter(predicate).findAny();
  }

  private static <E extends Enum<E> & Findable<E>> Optional<E> find(Class<E> clazz, ByteBuffer buffer) {
    return find(clazz, e -> e.find(buffer.get(ProtocolByte.TYPE.ordinal())).filter(o -> o == e).isPresent());
  }

  public static class RequestBuilder extends AbstractCheckedBuilder<RsceCommandFrame> {
    @Nonnegative
    private byte codeLength;

    public RequestBuilder(Control control, ActionType actionType, RequestType requestType) {
      super(ByteBuffer.allocate(MAX_CAPACITY).order(ByteOrder.LITTLE_ENDIAN));
      codeLength = 3;
      buffer().put(control.addr).put(codeLength).put((byte) ((actionType.ordinal() << 3) + (requestType.code & 0xff)));
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

  public static class ResponseBuilder extends AbstractCheckedBuilder<Optional<RsceCommandFrame>> {
    public ResponseBuilder() {
      this(ByteBuffer.allocate(MAX_CAPACITY));
    }

    public ResponseBuilder(ByteBuffer buffer) {
      super(buffer.order(ByteOrder.LITTLE_ENDIAN));
    }

    @Override
    public boolean is(byte b) {
      return buffer().position() > ProtocolByte.values().length || ProtocolByte.values()[buffer().position() - 1].isCheckedAndLimitSet(b, buffer());
    }

    @Override
    public Optional<RsceCommandFrame> build() {
      if (buffer().position() == 0) {
        for (ProtocolByte protocolByte : ProtocolByte.values()) {
          if (!protocolByte.is(buffer().get())) {
            logWarning();
            return Optional.empty();
          }
        }
      }

      int codeLength = buffer().limit() - NON_LEN_BYTES;
      if (buffer().getShort(codeLength) == (short) getChecksum(buffer(), codeLength)) {
        return Optional.of(new RsceCommandFrame(this));
      }
      else {
        logWarning();
        return Optional.empty();
      }
    }
  }

  private static long getChecksum(ByteBuffer buffer, @Nonnegative int codeLength) {
    Checksum checksum = new CRC16IBMChecksum();
    checksum.update(buffer.array(), 0, codeLength);
    return checksum.getValue();
  }

  private static String toType(ByteBuffer byteBuffer) {
    return toType(Control.find(byteBuffer), ActionType.find(byteBuffer), RequestType.find(byteBuffer));
  }

  private static String toType(Control control, ActionType actionType, RequestType requestType) {
    return String.join(Strings.SPACE, control.name(), actionType.name(), requestType.name());
  }
}
