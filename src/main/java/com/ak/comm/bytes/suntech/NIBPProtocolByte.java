package com.ak.comm.bytes.suntech;

import java.nio.ByteBuffer;

import javax.annotation.Nonnull;

import com.ak.comm.bytes.BytesChecker;

enum NIBPProtocolByte implements BytesChecker {
  START {
    @Override
    public boolean is(byte b) {
      return b == 0x3E;
    }
  },
  LEN {
    @Override
    public boolean is(byte b) {
      return b > 1 && b <= MAX_CAPACITY;
    }

    @Override
    public void bufferLimit(@Nonnull ByteBuffer buffer) {
      buffer.limit(buffer.get(ordinal()));
    }
  },
  DATA {
    @Override
    public boolean is(byte b) {
      return true;
    }
  };

  static final int MAX_CAPACITY = 0x43;

  static boolean checkCRC(@Nonnull ByteBuffer byteBuffer) {
    int crc = 0;
    byteBuffer.rewind();
    for (int i = 0; i < byteBuffer.limit(); i++) {
      crc += byteBuffer.get();
    }
    byteBuffer.rewind();
    return (byte) (crc & 0xff) == 0;
  }
}
