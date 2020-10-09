package com.ak.comm.bytes.suntech;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.stream.IntStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.ak.comm.bytes.AbstractCheckedBuilder;
import com.ak.comm.bytes.BufferFrame;

import static com.ak.comm.bytes.suntech.NIBPProtocolByte.MAX_CAPACITY;
import static com.ak.comm.bytes.suntech.NIBPProtocolByte.checkCRC;

public class NIBPResponse extends BufferFrame {
  private NIBPResponse(Builder builder) {
    super(builder.buffer());
  }

  public IntStream extractPressure() {
    if (get(NIBPProtocolByte.LEN.ordinal()) == 5) {
      return IntStream.of(byteBuffer().getShort(NIBPProtocolByte.DATA.ordinal()));
    }
    else {
      return IntStream.empty();
    }
  }

  public static class Builder extends AbstractCheckedBuilder<NIBPResponse> {
    public Builder() {
      this(ByteBuffer.allocate(MAX_CAPACITY));
    }

    public Builder(@Nonnull ByteBuffer buffer) {
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
