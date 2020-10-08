package com.ak.comm.bytes.suntech;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.ak.comm.bytes.AbstractCheckedBuilder;

import static com.ak.comm.bytes.suntech.NIBPProtocolByte.MAX_CAPACITY;
import static com.ak.comm.bytes.suntech.NIBPProtocolByte.checkCRC;

public class NIBPResponse {
  private NIBPResponse(ResponseBuilder responseBuilder) {
    System.out.println("NIBPResponse.NIBPResponse");
  }

  public static class ResponseBuilder extends AbstractCheckedBuilder<NIBPResponse> {
    public ResponseBuilder() {
      this(ByteBuffer.allocate(MAX_CAPACITY));
    }

    public ResponseBuilder(@Nonnull ByteBuffer buffer) {
      super(buffer.order(ByteOrder.LITTLE_ENDIAN));
    }

    @Override
    public boolean is(byte b) {
      return buffer().position() > NIBPProtocolByte.values().length || NIBPProtocolByte.values()[buffer().position() - 1].isCheckedAndLimitSet(b, buffer());
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
