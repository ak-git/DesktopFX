package com.ak.appliance.nmis.comm.bytes;

import com.ak.comm.bytes.AbstractCheckedBuilder;
import com.ak.comm.bytes.BufferFrame;
import com.ak.util.Strings;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;

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
  private final NmisAddress address;

  private NmisResponseFrame(ByteBuffer byteBuffer, NmisAddress address) {
    super(byteBuffer);
    this.address = Objects.requireNonNull(address);
  }

  /**
   * <pre>
   *   0х7Е, 0х45 (address for wrapped frame type), Len, CounterLow, CounterHi, DATA_WRAPPED_RSC_Energia ..., CRC
   * </pre>
   * Examples:
   * <pre>
   *   NmisResponseFrame[ 0x7e 0x45 0x02 <b>0x80 0x00</b> 0x45 ] DATA
   *   NmisResponseFrame[ 0x7e 0x45 0x09 <b>0x85 0x00</b> 0x01 0x05 0x0b 0xe0 0xb1 0xe1 0x7a 0x4e ] DATA
   * </pre>
   */
  public IntStream extractTime() {
    if (NmisAddress.DATA == address) {
      return IntStream.of(byteBuffer().getShort(NmisProtocolByte.DATA_1.ordinal()));
    }
    else {
      return IntStream.empty();
    }
  }

  /**
   * <pre>
   *   0х7Е, 0х45 (address for wrapped frame type), Len, CounterLow, CounterHi, DATA_WRAPPED_RSC_Energia ..., CRC
   * </pre>
   * Examples:
   * <pre>
   *   NmisResponseFrame[ 0x7e 0x45 0x02 0x80 0x00 0x45 ] DATA
   *   NmisResponseFrame[ 0x7e 0x45 0x09 0x85 0x00 <b>0x01 0x05 0x0b 0xe0 0xb1 0xe1 0x7a</b> 0x4e ] DATA
   * </pre>
   */
  public void extractData(ByteBuffer destination) {
    if (NmisAddress.DATA == address) {
      int len = byteBuffer().get(NmisProtocolByte.LEN.ordinal()) - 2;
      if (len > 0) {
        destination.put(byteBuffer().array(), NmisProtocolByte.DATA_3.ordinal(), len);
      }
    }
  }

  @Override
  public String toString() {
    return String.join(Strings.SPACE, super.toString(), address.toString());
  }

  public static class Builder extends AbstractCheckedBuilder<Optional<NmisResponseFrame>> {
    public Builder() {
      this(ByteBuffer.allocate(NmisProtocolByte.MAX_CAPACITY));
    }

    public Builder(ByteBuffer buffer) {
      super(buffer.order(ByteOrder.LITTLE_ENDIAN));
    }

    @Override
    public boolean is(byte b) {
      var okFlag = true;
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

    @Override
    public Optional<NmisResponseFrame> build() {
      if (buffer().position() == 0) {
        for (NmisProtocolByte protocolByte : NmisProtocolByte.CHECKED_BYTES) {
          if (!protocolByte.is(buffer().get(protocolByte.ordinal()))) {
            logWarning();
            return Optional.empty();
          }
        }
      }

      var address = NmisAddress.find(buffer());
      if (address.isPresent()) {
        if (NmisProtocolByte.checkCRC(buffer())) {
          return Optional.of(new NmisResponseFrame(buffer(), address.orElseThrow()));
        }
        logWarning();
      }
      return Optional.empty();
    }
  }
}
