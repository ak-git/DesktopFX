package com.ak.comm.interceptor;

import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

import com.ak.comm.core.Service;

public interface BytesInterceptor<FROM, TO> extends WritableByteChannel, Service<FROM> {
  @Override
  int write(ByteBuffer src);

  TO getStartCommand();

  ByteBuffer put(TO to);
}
