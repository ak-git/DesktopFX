package com.ak.comm.bytes.suntech;

import java.nio.ByteOrder;

import javax.annotation.Nonnull;

import com.ak.comm.bytes.BufferFrame;

public final class NIBPRequest extends BufferFrame {
  public static final NIBPRequest START_BP = new NIBPRequest(new byte[] {0x3A, 0x20, (byte) 0xA6});
  public static final NIBPRequest GET_CUFF_PRESSURE = new NIBPRequest(new byte[] {0x3A, 0x79, 0x05, 0x00, 0x48});
  public static final NIBPRequest GET_BP_DATA = new NIBPRequest(new byte[] {0x3A, 0x79, 0x03, 0x00, 0x4A});

  private NIBPRequest(@Nonnull byte[] bytes) {
    super(bytes, ByteOrder.LITTLE_ENDIAN);
  }
}
