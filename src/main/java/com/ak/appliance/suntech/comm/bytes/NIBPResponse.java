package com.ak.appliance.suntech.comm.bytes;

import com.ak.comm.bytes.AbstractCheckedBuilder;
import com.ak.comm.bytes.BufferFrame;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

import static com.ak.appliance.suntech.comm.bytes.NIBPProtocolByte.MAX_CAPACITY;
import static com.ak.appliance.suntech.comm.bytes.NIBPProtocolByte.checkCRC;

public class NIBPResponse extends BufferFrame {
  private NIBPResponse(Builder builder) {
    super(builder.buffer());
  }

  public void extractPressure(IntConsumer ifExist) {
    if (get(NIBPProtocolByte.LEN.ordinal()) == 5) {
      ifExist.accept(byteBuffer().getShort(NIBPProtocolByte.DATA.ordinal()));
    }
  }

  public void extractIsCompleted(Runnable ifExist) {
    if (get(NIBPProtocolByte.LEN.ordinal()) == 4 && get(NIBPProtocolByte.DATA.ordinal()) == 0x4b) {
      ifExist.run();
    }
  }

  public void extractData(Consumer<int[]> ifExist) {
    if (get(NIBPProtocolByte.LEN.ordinal()) == 0x18) {
      ifExist.accept(new int[] {
          byteBuffer().getShort(2),
          byteBuffer().getShort(2 + 2),
          Byte.toUnsignedInt(byteBuffer().get(2 + 2 + 2 + 1 + 1 + 8)),
          byteBuffer().getShort(2 + 2 + 2 + 1 + 1 + 8 + 1 + 1)
      });
    }
  }

  public static class Builder extends AbstractCheckedBuilder<Optional<NIBPResponse>> {
    public Builder() {
      this(ByteBuffer.allocate(MAX_CAPACITY));
    }

    public Builder(ByteBuffer buffer) {
      super(buffer.order(ByteOrder.LITTLE_ENDIAN));
    }

    @Override
    public boolean is(byte b) {
      return buffer().position() > NIBPProtocolByte.values().length ||
          NIBPProtocolByte.values()[buffer().position() - 1].isCheckedAndLimitSet(b, buffer());
    }

    @Override
    public Optional<NIBPResponse> build() {
      if (buffer().position() == 0) {
        for (NIBPProtocolByte protocolByte : NIBPProtocolByte.values()) {
          if (!protocolByte.is(buffer().get())) {
            logWarning();
            return Optional.empty();
          }
        }
      }

      if (checkCRC(buffer())) {
        return Optional.of(new NIBPResponse(this));
      }
      else {
        logWarning();
        return Optional.empty();
      }
    }
  }
}
