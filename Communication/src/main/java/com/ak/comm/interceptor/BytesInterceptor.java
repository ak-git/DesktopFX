package com.ak.comm.interceptor;

import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.ak.comm.core.Service;

public interface BytesInterceptor<RESPONSE, REQUEST> extends WritableByteChannel, Service<RESPONSE> {
  @Nonnull
  String name();

  @Nonnegative
  int getBaudRate();

  /**
   * Process input bytes buffer.<br/>
   * <b>REWIND bytes buffer before use!</b>
   *
   * @param src input bytes buffer
   * @return count response generated
   */
  @Override
  int write(@Nonnull ByteBuffer src);

  @Nullable
  REQUEST getPingRequest();

  /**
   * Converts object to bytes and puts them into output buffer.
   *
   * @param request an object to convert and send out
   * @return output bytes buffer with object converted
   */
  @Nonnull
  ByteBuffer put(@Nonnull REQUEST request);
}
