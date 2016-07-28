package com.ak.hardware.nmis.comm.interceptor;

import java.nio.ByteBuffer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.ak.comm.interceptor.AbstractBufferFrame;
import com.ak.comm.interceptor.AbstractCheckedBuilder;

/**
 * Classic <b>NMI Test Stand</b> Response Frame for INEUM protocol.
 */
public final class NmisResponseFrame extends AbstractBufferFrame {
  @Nonnull
  private final NmisAddress address;

  private NmisResponseFrame(@Nonnull ByteBuffer byteBuffer, @Nonnull NmisAddress address) {
    super(byteBuffer);
    this.address = address;
  }

  @Nonnull
  @Override
  public String toString() {
    return String.format("%s %s", super.toString(), address);
  }

  static class Builder extends AbstractCheckedBuilder<NmisResponseFrame> {
    Builder() {
      this(ByteBuffer.allocate(NmisProtocolByte.MAX_CAPACITY));
    }

    Builder(@Nonnull ByteBuffer buffer) {
      super(buffer);
    }

    @Override
    public boolean is(byte b) {
      boolean okFlag = true;

      for (NmisProtocolByte protocolByte : NmisProtocolByte.CHECKED_BYTES) {
        if (buffer().position() - 1 == protocolByte.ordinal()) {
          if (protocolByte.is(b)) {
            protocolByte.buffer(buffer());
          }
          else {
            okFlag = false;
          }
          break;
        }
      }
      return okFlag;

    }

    @Nullable
    @Override
    public NmisResponseFrame build() {
      if (buffer().position() == 0) {
        for (NmisProtocolByte protocolByte : NmisProtocolByte.CHECKED_BYTES) {
          if (!protocolByte.is(buffer().get(protocolByte.ordinal()))) {
            logWarning(null);
            return null;
          }
        }
      }

      NmisAddress address = NmisAddress.find(buffer());
      if (address != null) {
        if (NmisProtocolByte.checkCRC(buffer())) {
          return new NmisResponseFrame(buffer(), address);
        }
        logWarning(null);
      }
      return null;
    }
  }
}
