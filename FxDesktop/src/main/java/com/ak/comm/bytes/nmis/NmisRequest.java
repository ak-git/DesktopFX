package com.ak.comm.bytes.nmis;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import com.ak.comm.bytes.AbstractBufferFrame;

import static com.ak.util.Strings.SPACE;

public final class NmisRequest extends AbstractBufferFrame {
  private enum Ohm {
    Z_360(0), Z_0_47(1), Z_1(1 << 1), Z_2(1 << 2), Z_30A(1 << 3), Z_30B(1 << 4), Z_127(1 << 5);

    private final byte code;

    Ohm(int code) {
      this.code = (byte) code;
    }
  }

  public enum MyoFrequency {
    OFF(0), HZ_50(1), HZ_100(1 << 1), HZ_200(1 << 2), HZ_500(1 << 3), HZ_1000(1 << 4), NOISE(1 << 5);

    private final byte code;

    MyoFrequency(int code) {
      this.code = (byte) code;
    }
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
      this.ohms = Arrays.copyOf(ohms, ohms.length);
    }

    public final NmisRequest buildForAll(MyoType myoType, MyoFrequency frequency) {
      return new Builder(NmisAddress.SINGLE).forAll(ohms).forAll(myoType, frequency).build();
    }
  }

  public enum Sequence implements javafx.util.Builder<NmisRequest> {
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
      return new Builder(NmisAddress.SEQUENCE).sequence(number).forAll(myoType, frequency).build();
    }
  }

  private final String toString;

  private NmisRequest(@Nonnull Builder builder) {
    super(builder.codes);
    toString = builder.toStringBuilder.toString();
  }

  NmisResponseFrame toResponse() {
    byte[] codes = Arrays.copyOf(byteBuffer().array(), byteBuffer().capacity());
    codes[NmisProtocolByte.ADDR.ordinal()] = Objects.requireNonNull(NmisAddress.find(byteBuffer())).getAddrResponse();
    saveCRC(codes);
    NmisResponseFrame response = new NmisResponseFrame.Builder(ByteBuffer.wrap(codes)).build();
    if (response == null) {
      throw new NullPointerException(Arrays.toString(codes));
    }
    return response;
  }

  @Override
  public String toString() {
    return String.format("%s %s", super.toString(), toString);
  }

  private static void saveCRC(@Nonnull byte[] codes) {
    codes[NmisProtocolByte.CRC.ordinal()] = 0;
    int crc = 0;
    for (byte code : codes) {
      crc += code;
    }
    codes[NmisProtocolByte.CRC.ordinal()] = (byte) (crc & 0xff);
  }

  private static class Builder implements javafx.util.Builder<NmisRequest> {
    private final byte[] codes = new byte[1 + 1 + 1 + 8 + 1];
    private final StringBuilder toStringBuilder = new StringBuilder();

    private Builder(@Nonnull NmisAddress address) {
      codes[NmisProtocolByte.START.ordinal()] = 0x7E;
      codes[NmisProtocolByte.ADDR.ordinal()] = address.getAddrRequest();
      codes[NmisProtocolByte.LEN.ordinal()] = 0x08;
      toStringBuilder.append(address.name()).append(SPACE);
    }

    Builder forAll(@Nonnull Ohm... ohms) {
      byte code = (byte) Stream.of(ohms).mapToInt(ohm -> ohm.code).sum();
      Arrays.fill(codes, NmisProtocolByte.DATA_1.ordinal(), NmisProtocolByte.DATA_4.ordinal() + 1, code);
      toStringBuilder.append(Arrays.toString(ohms)).append(SPACE);
      return this;
    }

    Builder forAll(@Nonnull MyoType myoType, @Nonnull MyoFrequency frequency) {
      Arrays.fill(codes, NmisProtocolByte.DATA_5.ordinal(), NmisProtocolByte.DATA_8.ordinal() + 1,
          (byte) (myoType.code + frequency.code));
      toStringBuilder.append(myoType.name()).append(SPACE).append(frequency.name()).append(SPACE);
      return this;
    }

    Builder sequence(int number) {
      Arrays.fill(codes, NmisProtocolByte.DATA_1.ordinal(), NmisProtocolByte.DATA_4.ordinal() + 1, (byte) 0x00);
      codes[NmisProtocolByte.DATA_1.ordinal()] = (byte) number;
      toStringBuilder.append(number).append(SPACE);
      return this;
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
