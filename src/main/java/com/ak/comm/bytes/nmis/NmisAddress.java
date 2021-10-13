package com.ak.comm.bytes.nmis;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.ak.comm.bytes.LogUtils;

public enum NmisAddress {
  SINGLE(0x81, 0x91),
  SEQUENCE(0x82, 0x92),
  ALIVE(0x00, 0x40),
  CATCH_ELBOW(0x00, 0x41),
  ROTATE_ELBOW(0x00, 0x42),
  CATCH_HAND(0x00, 0x43),
  ROTATE_HAND(0x00, 0x44),
  DATA(0x45, 0x45);

  public static final Collection<NmisAddress> CHANNELS = Collections.unmodifiableCollection(
      EnumSet.of(CATCH_ELBOW, ROTATE_ELBOW, CATCH_HAND, ROTATE_HAND));

  private final byte addrRequest;
  private final byte addrResponse;

  NmisAddress(int addrRequest, int addrResponse) {
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
  static NmisAddress find(@Nonnull ByteBuffer byteBuffer) {
    var addr = byteBuffer.get(NmisProtocolByte.ADDR.ordinal());
    for (NmisAddress nmisAddress : values()) {
      if (nmisAddress.addrRequest == addr || nmisAddress.addrResponse == addr) {
        return nmisAddress;
      }
    }
    Logger.getLogger(NmisAddress.class.getName()).log(LogUtils.LOG_LEVEL_ERRORS,
        () -> "%s Address %d not found".formatted(LogUtils.toString(NmisAddress.class, byteBuffer), addr));
    return null;
  }
}
