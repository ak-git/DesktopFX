package com.ak.hardware.tnmi.comm.interceptor;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

enum TnmiAddress {
  SINGLE(0x81, 0x91),
  SEQUENCE(0x82, 0x92),
  ALIVE(0x00, 0x40),
  CATCH_ELBOW(0x00, 0x41),
  ROTATE_ELBOW(0x00, 0x42),
  CATCH_HAND(0x00, 0x43),
  ROTATE_HAND(0x00, 0x44),
  DATA(0x45, 0x45);

  public static final Collection<TnmiAddress> CHANNELS = Collections.unmodifiableCollection(
      EnumSet.of(CATCH_ELBOW, ROTATE_ELBOW, CATCH_HAND, ROTATE_HAND));

  private final byte addrRequest;
  private final byte addrResponse;

  TnmiAddress(int addrRequest, int addrResponse) {
    this.addrRequest = (byte) addrRequest;
    this.addrResponse = (byte) addrResponse;
  }

  final byte getAddrRequest() {
    if (addrRequest == ALIVE.addrRequest) {
      throw new UnsupportedOperationException(name());
    }
    else {
      return addrRequest;
    }
  }

  final byte getAddrResponse() {
    return addrResponse;
  }

  @Nullable
  static TnmiAddress find(@Nonnull byte[] codes) {
    byte addr = codes[TnmiProtocolByte.ADDR.ordinal()];
    for (TnmiAddress tnmiAddress : values()) {
      if (tnmiAddress.addrRequest == addr || tnmiAddress.addrResponse == addr) {
        return tnmiAddress;
      }
    }
    Logger.getLogger(TnmiAddress.class.getName()).log(Level.WARNING, String.format("Address %d not found: %s", addr, Arrays.toString(codes)));
    return null;
  }
}
