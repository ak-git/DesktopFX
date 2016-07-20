package com.ak.hardware.nmis.comm.interceptor;

import java.nio.ByteBuffer;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;

import com.ak.comm.interceptor.AbstractBufferFrame;

/**
 * Classic <b>NMI Test Stand</b> Response Frame for INEUM protocol.
 */
@Immutable
@ThreadSafe
public final class NmisResponseFrame extends AbstractBufferFrame {
  @Nonnull
  private final NmisAddress address;

  private NmisResponseFrame(@Nonnull ByteBuffer byteBuffer) {
    super(byteBuffer);
    address = Objects.requireNonNull(NmisAddress.find(byteBuffer));
  }

  @Nullable
  static NmisResponseFrame newInstance(@Nonnull ByteBuffer byteBuffer) {
    if (NmisAddress.find(byteBuffer) != null) {
      if (NmisProtocolByte.checkCRC(byteBuffer)) {
        for (NmisProtocolByte b : NmisProtocolByte.CHECKED_BYTES) {
          if (!b.is(byteBuffer.get(b.ordinal()))) {
            logWarning(byteBuffer, null);
            return null;
          }
        }
        return new NmisResponseFrame(byteBuffer);
      }
      logWarning(byteBuffer, null);
    }
    return null;
  }

  @Nonnull
  @Override
  public String toString() {
    return String.format("%s %s", super.toString(), address);
  }
}
