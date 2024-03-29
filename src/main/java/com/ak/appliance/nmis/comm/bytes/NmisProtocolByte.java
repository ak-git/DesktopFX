package com.ak.appliance.nmis.comm.bytes;

import com.ak.comm.bytes.BytesChecker;

import java.nio.ByteBuffer;

public enum NmisProtocolByte implements BytesChecker {
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
    public void bufferLimit(ByteBuffer buffer) {
      buffer.limit(buffer.get(ordinal()) + 4);
    }
  }, DATA_1, DATA_2, DATA_3, DATA_4, DATA_5, DATA_6, DATA_7, DATA_8, CRC;

  public static final int MAX_CAPACITY = 64;
  static final NmisProtocolByte[] CHECKED_BYTES = {START, LEN};

  static boolean checkCRC(ByteBuffer byteBuffer) {
    var crc = 0;
    byteBuffer.rewind();
    for (var i = 0; i < byteBuffer.limit() - 1; i++) {
      crc += byteBuffer.get();
    }
    byteBuffer.rewind();
    return byteBuffer.get(byteBuffer.limit() - 1) == (byte) (crc & 0xff);
  }
}
