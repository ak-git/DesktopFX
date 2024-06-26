package com.ak.appliance.nmis.comm.bytes;

import com.ak.comm.bytes.LogUtils;

import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.logging.Logger;

public enum NmisAddress {
  SINGLE(0x81, 0x91),
  SEQUENCE(0x82, 0x92),
  ALIVE(0x00, 0x40),
  CATCH_ELBOW(0x00, 0x41),
  ROTATE_ELBOW(0x00, 0x42),
  CATCH_HAND(0x00, 0x43),
  ROTATE_HAND(0x00, 0x44),
  DATA(0x45, 0x45);

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

  static Optional<NmisAddress> find(ByteBuffer byteBuffer) {
    byte addr = byteBuffer.get(NmisProtocolByte.ADDR.ordinal());
    for (NmisAddress nmisAddress : values()) {
      if (nmisAddress.addrRequest == addr || nmisAddress.addrResponse == addr) {
        return Optional.of(nmisAddress);
      }
    }
    Logger.getLogger(NmisAddress.class.getName()).log(LogUtils.LOG_LEVEL_ERRORS,
        () -> "%s Address %d not found".formatted(LogUtils.toString(NmisAddress.class, byteBuffer), addr));
    return Optional.empty();
  }
}
