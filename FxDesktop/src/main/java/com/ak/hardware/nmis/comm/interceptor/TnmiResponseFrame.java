package com.ak.hardware.nmis.comm.interceptor;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;

import com.ak.comm.interceptor.AbstractBufferFrame;

/**
 * Classic TNMI Response Frame for INEUM protocol.
 */
@Immutable
@ThreadSafe
public final class TnmiResponseFrame {
  private final TnmiAddress address;
  private final ByteBuffer buffer;

  private TnmiResponseFrame(@Nonnull byte[] bytes) {
    address = Objects.requireNonNull(TnmiAddress.find(bytes));
    buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
    buffer.flip();
  }

  @Nullable
  static TnmiResponseFrame newInstance(@Nonnull byte[] bytes) {
    if (TnmiAddress.find(bytes) != null && TnmiProtocolByte.checkCRC(bytes)) {
      for (TnmiProtocolByte b : TnmiProtocolByte.CHECKED_BYTES) {
        if (!b.is(bytes[b.ordinal()])) {
          logWarning(bytes);
          return null;
        }
      }
      return new TnmiResponseFrame(bytes);
    }
    logWarning(bytes);
    return null;
  }

  @Nonnull
  @Override
  public String toString() {
    return String.format("%s %s", AbstractBufferFrame.toString(getClass(), buffer.array()), address);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof TnmiResponseFrame)) {
      return false;
    }

    TnmiResponseFrame response = (TnmiResponseFrame) o;
    return buffer.equals(response.buffer);
  }

  @Override
  public int hashCode() {
    return buffer.hashCode();
  }

  private static void logWarning(@Nonnull byte[] array) {
    Logger.getLogger(TnmiResponseFrame.class.getName()).log(Level.CONFIG,
        String.format("Invalid TNMI response format: {%s}", Arrays.toString(array)));
  }
}
