package com.ak.comm.bytes.sktbpr;

import com.ak.comm.bytes.BytesChecker;

import java.nio.ByteBuffer;

public enum SKTBProtocolByte implements BytesChecker {
  START {
    @Override
    public boolean is(byte b) {
      return b == (byte) 0xa5;
    }
  },
  ID,
  LEN {
    @Override
    public boolean is(byte b) {
      return b == 6;
    }

    @Override
    public void bufferLimit(ByteBuffer buffer) {
      buffer.limit(SKTBProtocolByte.values().length);
    }
  }, ROTATE_ANGLE_1, ROTATE_ANGLE_2, ROTATE_ANGLE_3, ROTATE_ANGLE_4, FLEX_ANGLE_1, FLEX_ANGLE_2;

  static final SKTBProtocolByte[] CHECKED_BYTES = {START, LEN};
}
