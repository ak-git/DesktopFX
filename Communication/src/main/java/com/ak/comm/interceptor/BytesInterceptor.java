package com.ak.comm.interceptor;

import java.nio.ByteBuffer;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface BytesInterceptor<RESPONSE, REQUEST> extends Function<ByteBuffer, Stream<RESPONSE>> {
  @Nonnegative
  int getBaudRate();

  /**
   * Process input bytes buffer.<br/>
   * <b>REWIND bytes buffer before use!</b>
   *
   * @param src input bytes buffer
   * @return response's publisher
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
