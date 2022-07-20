package com.ak.comm.interceptor.rsce;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.ak.comm.bytes.rsce.RsceCommandFrame;
import com.ak.comm.interceptor.BytesInterceptor;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static com.ak.comm.bytes.rsce.RsceCommandFrame.ActionType.NONE;
import static com.ak.comm.bytes.rsce.RsceCommandFrame.Control.CATCH;
import static com.ak.comm.bytes.rsce.RsceCommandFrame.RequestType.EMPTY;
import static com.ak.comm.bytes.rsce.RsceCommandFrame.RequestType.STATUS_I;
import static com.ak.comm.bytes.rsce.RsceCommandFrame.RequestType.STATUS_I_ANGLE;
import static com.ak.comm.bytes.rsce.RsceCommandFrame.RequestType.STATUS_I_SPEED;
import static com.ak.comm.bytes.rsce.RsceCommandFrame.RequestType.STATUS_I_SPEED_ANGLE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class RsceBytesInterceptor2Test {
  private final BytesInterceptor<RsceCommandFrame, RsceCommandFrame> interceptor = new RsceBytesInterceptor();
  private final ByteBuffer byteBuffer = ByteBuffer.allocate(1);

  static Stream<Arguments> data() {
    return Stream.of(
        //added 0x00 at start
        arguments(new byte[] {0x00, 0x01, 0x03, 0x00, 0x20, (byte) 0xF0}, RsceCommandFrame.simple(CATCH, EMPTY)),
        //added 0x00 at start and duplicate start byte 0x01
        arguments(new byte[] {0x00, 0x01, 0x01, 0x03, 0x01, (byte) 0xE1, 0x30}, RsceCommandFrame.simple(CATCH, STATUS_I)),
        //added 0x00 at start and duplicate bytes 0x01, 0x03 and add 0xff
        arguments(new byte[] {0x00, 0x01, 0x03, (byte) 0xff, 0x01, 0x04, (byte) 0xff, 0x01, 0x05, (byte) 0xff, 0x01, 0x06, (byte) 0xff,
                0x01, 0x07, (byte) 0xff,
                0x01, 0x0a, 0x04, 0x04, (byte) 0xE0, (byte) 0xB1, 0x40, 0x43, (byte) 0xEE, 0x22, 0x58, (byte) 0x86},
            new RsceCommandFrame.RequestBuilder(CATCH, NONE, STATUS_I_SPEED_ANGLE).addParam((byte) 0x04).
                addParam((short) 45536).addParam((short) 17216).addParam((short) 8942).build()),

        arguments(new byte[] {0x01, 0x03, 0x02, (byte) 0xA1, 0x31}, RsceCommandFrame.simple(CATCH, STATUS_I_SPEED)),
        //error in CRC16
        arguments(new byte[] {0x01, 0x03, 0x03, 0x60, (byte) 0xF1 + 1}, null),
        arguments(new byte[] {0x01, 0x03, 0x04, 0x21, 0x33}, RsceCommandFrame.simple(CATCH, STATUS_I_SPEED_ANGLE)),
        arguments(new byte[] {0x01, 0x08, 0x03, 0x01, 0x09, 0x0F, (byte) 0x9F, (byte) 0xA2, (byte) 0xEE, 0x22},
            new RsceCommandFrame.RequestBuilder(CATCH, NONE, STATUS_I_ANGLE).
                addParam((byte) 0x01).addParam((short) 3849).addParam((short) 41631).build()),

        arguments(new byte[] {0x01, 0x05, 0x0C, 0x00, 0x00, (byte) 0xD9, 0x0F}, RsceCommandFrame.precise(CATCH, STATUS_I_SPEED_ANGLE, (short) 0)),
        arguments(new byte[] {0x01, 0x05, 0x0C, 0x20, 0x4E, 0x40, (byte) 0xFB}, RsceCommandFrame.precise(CATCH, STATUS_I_SPEED_ANGLE, (short) 20000)),
        arguments(new byte[] {0x01, 0x05, 0x0C, (byte) 0xE0, (byte) 0xB1, 0x50, (byte) 0xBB}, RsceCommandFrame.precise(CATCH, STATUS_I_SPEED_ANGLE, (short) 45536)),
        arguments(new byte[] {0x01, 0x05, 0x0C, (byte) 0xA0, 0x0F, (byte) 0xE1, 0x0B}, RsceCommandFrame.precise(CATCH, STATUS_I_SPEED_ANGLE, (short) 4000)),

        arguments(new byte[] {0x01, 0x06, 0x01, 0x01, 0x09, 0x0F, (byte) 0x9F, (byte) 0xA2},
            new RsceCommandFrame.RequestBuilder(CATCH, NONE, STATUS_I).addParam((byte) 0x01).addParam((short) 3849).build()),
        arguments(new byte[] {0x01, 0x06, 0x01, 0x02, (byte) 0xD9, 0x0F, 0x32, 0x62},
            new RsceCommandFrame.RequestBuilder(CATCH, NONE, STATUS_I).addParam((byte) 0x02).addParam((short) 4057).build()),
        arguments(new byte[] {0x01, 0x06, 0x01, 0x03, 0x40, (byte) 0xFB, 0x08, 0x75},
            new RsceCommandFrame.RequestBuilder(CATCH, NONE, STATUS_I).addParam((byte) 0x03).addParam((short) 64320).build()),
        arguments(new byte[] {0x01, 0x06, 0x01, 0x04, (byte) 0xE0, (byte) 0xB1, 0x40, 0x43},
            new RsceCommandFrame.RequestBuilder(CATCH, NONE, STATUS_I).addParam((byte) 0x04).addParam((short) 45536).build()),

        arguments(new byte[] {0x01, 0x08, 0x02, 0x01, 0x09, 0x0F, (byte) 0x9F, (byte) 0xA2, (byte) 0xEF, (byte) 0xF3},
            new RsceCommandFrame.RequestBuilder(CATCH, NONE, STATUS_I_SPEED).addParam((byte) 0x01).addParam((short) 3849).addParam((short) 41631).build()),
        arguments(new byte[] {0x01, 0x08, 0x02, 0x02, (byte) 0xD9, 0x0F, 0x03, 0x00, 0x7B, (byte) 0x8A},
            new RsceCommandFrame.RequestBuilder(CATCH, NONE, STATUS_I_SPEED).addParam((byte) 0x02).addParam((short) 4057).addParam((short) 3).build()),
        arguments(new byte[] {0x01, 0x08, 0x02, 0x03, 0x40, (byte) 0xFB, 0x00, 0x75, (byte) 0xE8, 0x33},
            new RsceCommandFrame.RequestBuilder(CATCH, NONE, STATUS_I_SPEED).addParam((byte) 0x03).addParam((short) 64320).addParam((short) 29952).build()),
        arguments(new byte[] {0x01, 0x08, 0x01, 0x04, (byte) 0xE0, (byte) 0xB1, 0x40, 0x43, (byte) 0xEF, (byte) 0xC0},
            new RsceCommandFrame.RequestBuilder(CATCH, NONE, STATUS_I).addParam((byte) 0x04).addParam((short) 45536).addParam((short) 17216).build()),

        arguments(new byte[] {0x01, 0x08, 0x03, 0x01, 0x09, 0x0F, (byte) 0x9F, (byte) 0xA2, (byte) 0xEE, 0x22},
            new RsceCommandFrame.RequestBuilder(CATCH, NONE, STATUS_I_ANGLE).addParam((byte) 0x01).addParam((short) 3849).addParam((short) 41631).build()),
        arguments(new byte[] {0x01, 0x08, 0x03, 0x02, (byte) 0xD9, 0x0F, 0x03, 0x00, 0x7A, 0x5B},
            new RsceCommandFrame.RequestBuilder(CATCH, NONE, STATUS_I_ANGLE).addParam((byte) 0x02).addParam((short) 4057).addParam((short) 3).build()),
        arguments(new byte[] {0x01, 0x08, 0x03, 0x03, 0x40, (byte) 0xFB, 0x00, 0x75, (byte) 0xE9, (byte) 0xE2},
            new RsceCommandFrame.RequestBuilder(CATCH, NONE, STATUS_I_ANGLE).addParam((byte) 0x03).addParam((short) 64320).addParam((short) 29952).build()),
        arguments(new byte[] {0x01, 0x08, 0x03, 0x04, (byte) 0xE0, (byte) 0xB1, 0x40, 0x43, (byte) 0xEE, 0x22},
            new RsceCommandFrame.RequestBuilder(CATCH, NONE, STATUS_I_ANGLE).addParam((byte) 0x04).addParam((short) 45536).addParam((short) 17216).build()),

        arguments(new byte[] {0x01, 0x0a, 0x04, 0x01, 0x09, 0x0F, (byte) 0x9F, (byte) 0xA2, (byte) 0xEE, 0x22, 0x58, (byte) 0x86},
            new RsceCommandFrame.RequestBuilder(CATCH, NONE, STATUS_I_SPEED_ANGLE).addParam((byte) 0x01).
                addParam((short) 3849).addParam((short) 41631).addParam((short) 8942).build()),
        arguments(new byte[] {0x01, 0x0a, 0x04, 0x02, (byte) 0xD9, 0x0F, 0x03, 0x00, 0x7A, 0x5B, 0x58, (byte) 0x86},
            new RsceCommandFrame.RequestBuilder(CATCH, NONE, STATUS_I_SPEED_ANGLE).addParam((byte) 0x02).
                addParam((short) 4057).addParam((short) 3).addParam((short) 23418).build()),
        arguments(new byte[] {0x01, 0x0a, 0x04, 0x03, 0x40, (byte) 0xFB, 0x00, 0x75, (byte) 0xE9, (byte) 0xE2, 0x58, (byte) 0x86},
            new RsceCommandFrame.RequestBuilder(CATCH, NONE, STATUS_I_SPEED_ANGLE).addParam((byte) 0x03).
                addParam((short) 64320).addParam((short) 29952).addParam((short) 58089).build()),
        arguments(new byte[] {0x01, 0x0a, 0x04, 0x04, (byte) 0xE0, (byte) 0xB1, 0x40, 0x43, (byte) 0xEE, 0x22, 0x58, (byte) 0x86},
            new RsceCommandFrame.RequestBuilder(CATCH, NONE, STATUS_I_SPEED_ANGLE).addParam((byte) 0x04).
                addParam((short) 45536).addParam((short) 17216).addParam((short) 8942).build()),

        //fake bytes
        arguments(new byte[] {(byte) 0xff, (byte) 0xff}, null)
    );
  }

  @ParameterizedTest
  @MethodSource("data")
  void testInterceptor(@Nonnull byte[] bytes, @Nullable RsceCommandFrame response) {
    Iterator<ByteBuffer> iterator = new Iterator<>() {
      int index;

      @Override
      public boolean hasNext() {
        return index < bytes.length;
      }

      @Override
      public ByteBuffer next() {
        byteBuffer.clear();
        byteBuffer.put(bytes[index]).flip();
        index++;
        return byteBuffer;
      }
    };

    while (iterator.hasNext()) {
      interceptor.apply(iterator.next()).forEach(rsceCommandFrame -> {
        assertThat(response).isNotNull().isEqualTo(rsceCommandFrame);
        assertThat(interceptor.putOut(response).remaining()).isPositive();
      });
    }
  }
}