package com.ak.appliance.nmis.comm.bytes;

import com.ak.comm.bytes.BufferFrame;
import com.ak.util.Builder;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.stream.Stream;

import static com.ak.util.Strings.SPACE;

public final class NmisRequest extends BufferFrame {
  private enum Ohm {
    Z_360, Z_0_47, Z_1, Z_2, Z_30A, Z_30B, Z_127
  }

  public enum MyoFrequency {
    OFF, HZ_50, HZ_100, HZ_200, HZ_500, HZ_1000, NOISE
  }

  public enum MyoType {
    OFF(0), MV0_1(1 << 6), MV1(1 << 7);

    private final byte code;

    MyoType(int code) {
      this.code = (byte) code;
    }
  }

  public enum Single {
    Z_360(Ohm.Z_360),
    Z_390(Ohm.Z_360, Ohm.Z_30A),
    Z_420(Ohm.Z_360, Ohm.Z_30A, Ohm.Z_30B),
    Z_422(Ohm.Z_360, Ohm.Z_30A, Ohm.Z_30B, Ohm.Z_2),
    Z_423(Ohm.Z_360, Ohm.Z_30A, Ohm.Z_30B, Ohm.Z_2, Ohm.Z_1),
    Z_423_47(Ohm.Z_360, Ohm.Z_30A, Ohm.Z_30B, Ohm.Z_2, Ohm.Z_1, Ohm.Z_0_47),
    Z_550_47(Ohm.values());

    private final Ohm[] ohms;

    Single(Ohm... ohms) {
      this.ohms = ohms.clone();
    }

    public final NmisRequest buildForAll(MyoType myoType, MyoFrequency frequency) {
      return new NmisRequestBuilder(NmisAddress.SINGLE).forAll(ohms).forAll(myoType, frequency).build();
    }
  }

  public enum Sequence implements Builder<NmisRequest> {
    CATCH_100(1, MyoType.MV1, MyoFrequency.HZ_200), CATCH_60(2, MyoType.MV1, MyoFrequency.HZ_200),
    CATCH_30(3, MyoType.MV1, MyoFrequency.HZ_200), CATCH_INV(4, MyoType.MV1, MyoFrequency.HZ_200),
    ROTATE_100(5, MyoType.MV0_1, MyoFrequency.NOISE), ROTATE_60(6, MyoType.MV0_1, MyoFrequency.NOISE),
    ROTATE_30(7, MyoType.MV0_1, MyoFrequency.NOISE), ROTATE_INV(8, MyoType.MV0_1, MyoFrequency.NOISE);

    private final int number;
    private final MyoType myoType;
    private final MyoFrequency frequency;

    Sequence(int number, MyoType myoType, MyoFrequency frequency) {
      this.number = number;
      this.myoType = myoType;
      this.frequency = frequency;
    }

    @Override
    public final NmisRequest build() {
      return new NmisRequestBuilder(NmisAddress.SEQUENCE).sequence(number).forAll(myoType, frequency).build();
    }
  }

  private final String toString;

  private NmisRequest(NmisRequestBuilder builder) {
    super(builder.codes, ByteOrder.LITTLE_ENDIAN);
    toString = builder.toStringBuilder.toString();
  }

  public NmisResponseFrame toResponse() {
    byte[] codes = Arrays.copyOf(byteBuffer().array(), byteBuffer().capacity());
    codes[NmisProtocolByte.ADDR.ordinal()] = NmisAddress.find(byteBuffer()).orElseThrow().getAddrResponse();
    saveCRC(codes);
    return new NmisResponseFrame.Builder(ByteBuffer.wrap(codes)).build().orElseThrow();
  }

  @Override
  public String toString() {
    return String.join(SPACE, super.toString(), toString);
  }

  private static void saveCRC(byte[] codes) {
    codes[NmisProtocolByte.CRC.ordinal()] = 0;
    var crc = 0;
    for (byte code : codes) {
      crc += code;
    }
    codes[NmisProtocolByte.CRC.ordinal()] = (byte) (crc & 0xff);
  }

  private static class NmisRequestBuilder implements Builder<NmisRequest> {
    private final byte[] codes = new byte[1 + 1 + 1 + 8 + 1];
    private final StringBuilder toStringBuilder = new StringBuilder();

    private NmisRequestBuilder(NmisAddress address) {
      codes[NmisProtocolByte.START.ordinal()] = 0x7E;
      codes[NmisProtocolByte.ADDR.ordinal()] = address.getAddrRequest();
      codes[NmisProtocolByte.LEN.ordinal()] = 0x08;
      toStringBuilder.append(address.name()).append(SPACE);
    }

    Builder<NmisRequest> forAll(MyoType myoType, MyoFrequency frequency) {
      Arrays.fill(codes, NmisProtocolByte.DATA_5.ordinal(), NmisProtocolByte.DATA_8.ordinal() + 1,
          (byte) (myoType.code + toCode(frequency)));
      toStringBuilder.append(myoType.name()).append(SPACE).append(frequency.name()).append(SPACE);
      return this;
    }

    NmisRequestBuilder forAll(Ohm... ohms) {
      byte code = (byte) Stream.of(ohms).mapToInt(NmisRequestBuilder::toCode).sum();
      Arrays.fill(codes, NmisProtocolByte.DATA_1.ordinal(), NmisProtocolByte.DATA_4.ordinal() + 1, code);
      toStringBuilder.append(Arrays.toString(ohms)).append(SPACE);
      return this;
    }

    NmisRequestBuilder sequence(int number) {
      Arrays.fill(codes, NmisProtocolByte.DATA_1.ordinal(), NmisProtocolByte.DATA_4.ordinal() + 1, (byte) 0x00);
      codes[NmisProtocolByte.DATA_1.ordinal()] = (byte) number;
      toStringBuilder.append(number).append(SPACE);
      return this;
    }

    private static byte toCode(Enum<?> e) {
      return (byte) (1 << (e.ordinal() - 1));
    }

    @Override
    public NmisRequest build() {
      if (toStringBuilder.length() > 1) {
        toStringBuilder.deleteCharAt(toStringBuilder.length() - 1);
      }
      saveCRC(codes);
      return new NmisRequest(this);
    }
  }
}
