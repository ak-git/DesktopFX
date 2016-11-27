package com.ak.comm.bytes.nmis;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.ak.comm.bytes.AbstractBufferFrame;

public enum NmisAddress {
  SINGLE(0x81, 0x91),
  SEQUENCE(0x82, 0x92),
  ALIVE(0x00, 0x40),
  CATCH_ELBOW(0x00, 0x41),
  ROTATE_ELBOW(0x00, 0x42),
  CATCH_HAND(0x00, 0x43),
  ROTATE_HAND(0x00, 0x44),
  DATA(0x45, 0x45);

  enum FrameField {
    NONE, TIME_COUNTER, DATA_WRAPPED
  }

  enum Extractor {
    NONE(DATA, FrameField.NONE),
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
    DATA_TIME(DATA, FrameField.TIME_COUNTER),
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
    DATA_DATA(DATA, FrameField.DATA_WRAPPED) {
      @Override
      void extract(@Nonnull ByteBuffer from, @Nonnull ByteBuffer to) {
        int len = from.get(NmisProtocolByte.LEN.ordinal()) - 2;
        if (len > 0) {
          to.put(from.array(), NmisProtocolByte.DATA_3.ordinal(), len);
        }
      }
    };

    private static final Map<NmisAddress, Map<FrameField, Extractor>> NMIS_ADDRESS_MAP = new EnumMap<>(NmisAddress.class);
    @Nonnull
    private final NmisAddress address;
    @Nonnull
    private final FrameField field;

    static {
      for (NmisAddress address : NmisAddress.values()) {
        NMIS_ADDRESS_MAP.put(address, new EnumMap<>(FrameField.class));
      }
      for (Extractor extractor : Extractor.values()) {
        NMIS_ADDRESS_MAP.get(extractor.address).put(extractor.field, extractor);
      }
    }

    Extractor(@Nonnull NmisAddress address, @Nonnull FrameField field) {
      this.address = address;
      this.field = field;
    }

    void extract(@Nonnull ByteBuffer from, @Nonnull ByteBuffer to) {
    }

    static Extractor from(@Nonnull NmisAddress address, @Nonnull FrameField field) {
      return Optional.ofNullable(NMIS_ADDRESS_MAP.get(address)).map(extractorMap -> extractorMap.get(field)).orElse(NONE);
    }
  }

  public static final Collection<NmisAddress> CHANNELS = Collections.unmodifiableCollection(
      EnumSet.of(CATCH_ELBOW, ROTATE_ELBOW, CATCH_HAND, ROTATE_HAND));

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

  @Nullable
  static NmisAddress find(@Nonnull ByteBuffer byteBuffer) {
    byte addr = byteBuffer.get(NmisProtocolByte.ADDR.ordinal());
    for (NmisAddress nmisAddress : values()) {
      if (nmisAddress.addrRequest == addr || nmisAddress.addrResponse == addr) {
        return nmisAddress;
      }
    }
    Logger.getLogger(NmisAddress.class.getName()).log(Level.CONFIG,
        String.format("%s Address %d not found", AbstractBufferFrame.toString(NmisAddress.class, byteBuffer), addr));
    return null;
  }
}
