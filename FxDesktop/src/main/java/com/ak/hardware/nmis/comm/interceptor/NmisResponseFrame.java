package com.ak.hardware.nmis.comm.interceptor;

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
  private final NmisAddress address;

  private NmisResponseFrame(@Nonnull byte[] bytes) {
    super(bytes);
    address = Objects.requireNonNull(NmisAddress.find(bytes));
  }

  @Nullable
  static NmisResponseFrame newInstance(@Nonnull byte[] bytes) {
    if (NmisAddress.find(bytes) != null) {
      if (NmisProtocolByte.checkCRC(bytes)) {
        for (NmisProtocolByte b : NmisProtocolByte.CHECKED_BYTES) {
          if (!b.is(bytes[b.ordinal()])) {
            logWarning(bytes, null);
            return null;
          }
        }
        return new NmisResponseFrame(bytes);
      }
      logWarning(bytes, null);
    }
    return null;
  }

  @Nonnull
  @Override
  public String toString() {
    return String.format("%s %s", super.toString(), address);
  }
}
