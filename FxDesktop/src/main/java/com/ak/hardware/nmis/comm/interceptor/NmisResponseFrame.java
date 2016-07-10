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
 * Classic <b>NMI Test Stand</b> Response Frame for INEUM protocol.
 */
@Immutable
@ThreadSafe
public final class NmisResponseFrame {
  private final NmisAddress address;
  private final ByteBuffer buffer;

  private NmisResponseFrame(@Nonnull byte[] bytes) {
    address = Objects.requireNonNull(NmisAddress.find(bytes));
    buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
    buffer.flip();
  }

  @Nullable
  static NmisResponseFrame newInstance(@Nonnull byte[] bytes) {
    if (NmisAddress.find(bytes) != null && NmisProtocolByte.checkCRC(bytes)) {
      for (NmisProtocolByte b : NmisProtocolByte.CHECKED_BYTES) {
        if (!b.is(bytes[b.ordinal()])) {
          logWarning(bytes);
          return null;
        }
      }
      return new NmisResponseFrame(bytes);
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
    if (!(o instanceof NmisResponseFrame)) {
      return false;
    }

    NmisResponseFrame response = (NmisResponseFrame) o;
    return buffer.equals(response.buffer);
  }

  @Override
  public int hashCode() {
    return buffer.hashCode();
  }

  private static void logWarning(@Nonnull byte[] array) {
    Logger.getLogger(NmisResponseFrame.class.getName()).log(Level.CONFIG,
        String.format("Invalid TNMI response format: {%s}", Arrays.toString(array)));
  }
}
