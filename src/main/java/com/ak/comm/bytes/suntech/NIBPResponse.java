package com.ak.comm.bytes.suntech;

import com.ak.comm.bytes.AbstractCheckedBuilder;
import com.ak.comm.bytes.BufferFrame;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

import static com.ak.comm.bytes.suntech.NIBPProtocolByte.MAX_CAPACITY;
import static com.ak.comm.bytes.suntech.NIBPProtocolByte.checkCRC;

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

  public static class Builder extends AbstractCheckedBuilder<NIBPResponse> {
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

    @Nullable
    @Override
    public NIBPResponse build() {
      if (buffer().position() == 0) {
        for (NIBPProtocolByte protocolByte : NIBPProtocolByte.values()) {
          if (!protocolByte.is(buffer().get())) {
            logWarning();
            return null;
          }
        }
      }

      if (checkCRC(buffer())) {
        return new NIBPResponse(this);
      }
      else {
        logWarning();
        return null;
      }
    }
  }
}
