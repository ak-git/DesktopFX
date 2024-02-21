package com.ak.comm.bytes.suntech;

import com.ak.comm.bytes.BufferFrame;

import java.nio.ByteOrder;

public final class NIBPRequest extends BufferFrame {
  public static final NIBPRequest START_BP = new NIBPRequest(new byte[] {0x3A, 0x20, (byte) 0xA6});
  public static final NIBPRequest GET_CUFF_PRESSURE = new NIBPRequest(new byte[] {0x3A, 0x79, 0x05, 0x00, 0x48});
  public static final NIBPRequest GET_BP_DATA = new NIBPRequest(new byte[] {0x3A, 0x79, 0x03, 0x00, 0x4A});
  public static final NIBPRequest CONTROL_PNEUMATICS_ALL_CLOSED = new NIBPRequest(new byte[] {0x3A, 0x0C, 0x00, 0x01, 0x01, (byte) 0xB8});

  private NIBPRequest(byte[] bytes) {
    super(bytes, ByteOrder.LITTLE_ENDIAN);
  }
}
