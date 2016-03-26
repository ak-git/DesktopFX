package com.ak.comm.interceptor;

import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

import com.ak.comm.core.Service;

public interface BytesInterceptor<RESPONSE, REQUEST> extends WritableByteChannel, Service<RESPONSE> {
  /**
   * Process input bytes buffer.<br/>
   * <b>REWIND bytes buffer before use!</b>
   *
   * @param src input bytes buffer
   * @return count bytes processed
   */
  @Override
  int write(ByteBuffer src);

  REQUEST getPingRequest();

  /**
   * Converts object to bytes and puts them into output buffer.
   *
   * @param request an object to convert and send out
   * @return output bytes buffer with object converted
   */
  ByteBuffer put(REQUEST request);
}
