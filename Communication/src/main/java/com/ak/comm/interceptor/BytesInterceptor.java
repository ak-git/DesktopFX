package com.ak.comm.interceptor;

import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

import com.ak.comm.core.Service;

public interface BytesInterceptor<T> extends WritableByteChannel, Service<T> {
  @Override
  int write(ByteBuffer src);
}
