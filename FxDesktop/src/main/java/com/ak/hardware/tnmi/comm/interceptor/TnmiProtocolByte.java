package com.ak.hardware.tnmi.comm.interceptor;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;

enum TnmiProtocolByte {
  START {
    @Override
    boolean is(byte b) {
      return b == 0x7E;
    }
  },
  ADDR,
  LEN {
    @Override
    boolean is(byte b) {
      return b > 1 && b <= MAX_CAPACITY;
    }
  }, DATA_1, DATA_2, DATA_3, DATA_4, DATA_5, DATA_6, DATA_7, DATA_8, CRC;

  static final int MAX_CAPACITY = 64;
  static final Collection<TnmiProtocolByte> CHECKED_BYTES = Collections.unmodifiableCollection(EnumSet.of(START, LEN));

  boolean is(byte b) {
    throw new UnsupportedOperationException(name());
  }

  static boolean checkCRC(byte[] codes) {
    int crc = 0;
    for (int i = 0; i < codes.length - 1; i++) {
      crc += codes[i];
    }
    return codes[codes.length - 1] == (byte) (crc & 0xff);
  }

  static String toString(Class<?> clazz, byte[] bytes) {
    StringBuilder sb = new StringBuilder(clazz.getSimpleName()).append("[ ");
    for (int i : bytes) {
      sb.append(String.format("%#04x ", (i & 0xFF)));
    }
    sb.append("]");
    return sb.toString();
  }
}
