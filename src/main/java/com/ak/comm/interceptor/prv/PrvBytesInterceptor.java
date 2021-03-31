package com.ak.comm.interceptor.prv;

import javax.annotation.Nonnull;
import javax.inject.Named;

import com.ak.comm.interceptor.simple.AbstractFixedFrameBytesInterceptor;
import org.springframework.context.annotation.Profile;

@Named
@Profile("prv")
public final class PrvBytesInterceptor extends AbstractFixedFrameBytesInterceptor {
  private static final byte START = '\n'; // 10
  private static final byte STOP = '\r'; // 13

  public PrvBytesInterceptor() {
    super("prv", BaudRate.BR_115200, 4 + 2);
  }

  @Override
  protected boolean check(@Nonnull byte[] buffer, byte nextFrameStartByte) {
    return buffer[0] == START && buffer[buffer.length - 1] == STOP && nextFrameStartByte == START;
  }
}
