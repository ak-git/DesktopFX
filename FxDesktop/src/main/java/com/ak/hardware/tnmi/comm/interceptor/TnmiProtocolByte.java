package com.ak.hardware.tnmi.comm.interceptor;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;

import javax.annotation.Nonnull;

import com.ak.comm.interceptor.BytesChecker;

public enum TnmiProtocolByte implements BytesChecker {
  START {
    @Override
    public boolean is(byte b) {
      return b == 0x7E;
    }
  },
  ADDR,
  LEN {
    @Override
    public boolean is(byte b) {
      return b > 1 && b <= MAX_CAPACITY - 4;
    }

    @Override
    public void buffer(byte b, @Nonnull ByteBuffer buffer) {
      buffer.limit(b + 4);
    }
  }, DATA_1, DATA_2, DATA_3, DATA_4, DATA_5, DATA_6, DATA_7, DATA_8, CRC;

  public static final int MAX_CAPACITY = 64;
  static final Collection<TnmiProtocolByte> CHECKED_BYTES = Collections.unmodifiableCollection(EnumSet.of(START, LEN));

  static boolean checkCRC(@Nonnull byte[] codes) {
    int crc = 0;
    for (int i = 0; i < codes.length - 1; i++) {
      crc += codes[i];
    }
    return codes[codes.length - 1] == (byte) (crc & 0xff);
  }
}
