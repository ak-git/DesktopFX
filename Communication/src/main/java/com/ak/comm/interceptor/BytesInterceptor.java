package com.ak.comm.interceptor;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface BytesInterceptor<RESPONSE, REQUEST> extends Function<ByteBuffer, Stream<RESPONSE>> {
  enum SerialParams {
    CLEAR_DTR
  }

  enum BaudRate {
    BR_38400(115200 / 3), BR_115200(115200), BR_460800(115200 * 4), BR_921600(115200 * 8);

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

  @Nonnull
  default Set<SerialParams> getSerialParams() {
    return Collections.emptySet();
  }

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
