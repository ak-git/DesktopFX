package com.ak.comm.bytes.nmis;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.ak.comm.bytes.AbstractCheckedBuilder;
import com.ak.comm.bytes.BufferFrame;

/**
 * Classic <b>NMI Test Stand</b> Response Frame for INEUM protocol.
 * Neuro-Muscular Interface Stand (Test Stand) Format:
 * <p>
 * <pre>
 *   <b>Start  Address Len Data1 Data2 Data3 Data4 Data5 Data6 Data7 Data8 CRC</b>
 *   0x7E   0xХХ    8   0xХХ  0xХХ  0xХХ  0xХХ  0xХХ  0xХХ  0xХХ  0xХХ  0xХХ
 * </pre>
 * <pre>
 *   <b>Test Stand Answer</b>
 *   0x7E (Start)
 *   0x41 (Address Channel 1)
 *   8 (Len)
 *   Velocity [DataL, DataH]
 *   Position [DataL, DataH]
 *   Reserved 0x00
 *   Reserved 0x00
 *   Time Delay [ms]
 *   0xXX (CRC)
 * </pre>
 * <p>
 * Also used <b>RSC Energia Hand Control</b> format <b>wrapped</b> by Neuro-Muscular Test Stand format.
 * <pre>
 *   0х7Е, 0х45 (address for wrapped frame type), Len, CounterLow, CounterHi, DATA_WRAPPED_RSC_Energia ..., CRC
 * </pre>
 * Examples:
 * <pre>
 *   NmisResponseFrame[ 0x7e 0x45 0x02 <b>0x80 0x00</b> 0x45 ] DATA
 *   NmisResponseFrame[ 0x7e 0x45 0x09 <b>0x85 0x00</b> 0x01 0x05 0x0b 0xe0 0xb1 0xe1 0x7a 0x4e ] DATA
 * </pre>
 * each 5 ms.
 */
public final class NmisResponseFrame extends BufferFrame {
  @Nonnull
  private final NmisAddress address;

  private NmisResponseFrame(@Nonnull ByteBuffer byteBuffer, @Nonnull NmisAddress address) {
    super(byteBuffer);
    this.address = address;
  }

  public void extractData(@Nonnull ByteBuffer destination) {
    NmisAddress.Extractor.from(address, NmisAddress.FrameField.DATA_WRAPPED).extract(byteBuffer(), destination);
  }

  @Override
  public String toString() {
    return String.format("%s %s", super.toString(), address);
  }

  public static class Builder extends AbstractCheckedBuilder<NmisResponseFrame> {
    public Builder() {
      this(ByteBuffer.allocate(NmisProtocolByte.MAX_CAPACITY).order(ByteOrder.LITTLE_ENDIAN));
    }

    public Builder(@Nonnull ByteBuffer buffer) {
      super(buffer);
    }

    @Override
    public boolean is(byte b) {
      boolean okFlag = true;
      for (NmisProtocolByte protocolByte : NmisProtocolByte.CHECKED_BYTES) {
        if (buffer().position() - 1 == protocolByte.ordinal()) {
          if (!protocolByte.isCheckedAndLimitSet(b, buffer())) {
            okFlag = false;
          }
          break;
        }
      }
      return okFlag;
    }

    @Nullable
    @Override
    public NmisResponseFrame build() {
      if (buffer().position() == 0) {
        for (NmisProtocolByte protocolByte : NmisProtocolByte.CHECKED_BYTES) {
          if (!protocolByte.is(buffer().get(protocolByte.ordinal()))) {
            logWarning();
            return null;
          }
        }
      }

      NmisAddress address = NmisAddress.find(buffer());
      if (address != null) {
        if (NmisProtocolByte.checkCRC(buffer())) {
          return new NmisResponseFrame(buffer(), address);
        }
        logWarning();
      }
      return null;
    }
  }
}
