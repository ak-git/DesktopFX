package com.ak.comm.interceptor;

import java.nio.ByteBuffer;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static jssc.SerialPort.BAUDRATE_115200;
import static jssc.SerialPort.BAUDRATE_38400;

public interface BytesInterceptor<RESPONSE, REQUEST> extends Function<ByteBuffer, Stream<RESPONSE>> {
  enum BaudRate {
    BR_38400(BAUDRATE_38400), BR_115200(BAUDRATE_115200), BR_460800(BAUDRATE_115200 * 4), BR_921600(BAUDRATE_115200 * 8);

    @Nonnegative
    private final int baudRate;

    BaudRate(int baudRate) {
      this.baudRate = baudRate;
    }

    @Nonnegative
    public final int get() {
      return baudRate;
    }
  }

  @Nonnegative
  int getBaudRate();

  /**
   * Process input bytes buffer.<br/>
   *
   * @param src input bytes buffer
   * @return response's stream
   */
  @Nonnull
  @Override
  Stream<RESPONSE> apply(@Nonnull ByteBuffer src);

  @Nullable
  REQUEST getPingRequest();

  /**
   * Converts object to bytes and puts them into output buffer.
   *
   * @param request an object to convert and send out
   * @return output bytes buffer with object converted
   */
  @Nonnull
  ByteBuffer putOut(@Nonnull REQUEST request);
}
