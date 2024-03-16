package com.ak.appliance.sktbpr.comm.bytes;

import com.ak.comm.bytes.AbstractCheckedBuilder;
import com.ak.comm.bytes.BufferFrame;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class SKTBResponse extends BufferFrame {
  private SKTBResponse(ByteBuffer byteBuffer) {
    super(byteBuffer);
  }

  public int rotateAngle() {
    return byteBuffer().getInt(SKTBProtocolByte.ROTATE_ANGLE_1.ordinal()) / 100;
  }

  public int flexAngle() {
    return byteBuffer().getShort(SKTBProtocolByte.FLEX_ANGLE_1.ordinal()) / 100;
  }

  public static class Builder extends AbstractCheckedBuilder<SKTBResponse> {
    public Builder() {
      super(ByteBuffer.allocate(SKTBProtocolByte.values().length).order(ByteOrder.LITTLE_ENDIAN));
    }

    @Override
    public boolean is(byte b) {
      var okFlag = true;
      for (SKTBProtocolByte protocolByte : SKTBProtocolByte.CHECKED_BYTES) {
        if (buffer().position() - 1 == protocolByte.ordinal()) {
          if (!protocolByte.isCheckedAndLimitSet(b, buffer())) {
            okFlag = false;
          }
          break;
        }
      }
      return okFlag;
    }

    @Nullable
    @Override
    public SKTBResponse build() {
      if (buffer().position() == 0) {
        for (SKTBProtocolByte protocolByte : SKTBProtocolByte.CHECKED_BYTES) {
          if (!protocolByte.is(buffer().get(protocolByte.ordinal()))) {
            logWarning();
            return null;
          }
        }
      }
      return new SKTBResponse(buffer());
    }
  }
}
