package com.ak.appliance.purelogic.comm.interceptor;

import com.ak.appliance.purelogic.comm.bytes.PureLogicFrame;
import com.ak.comm.interceptor.AbstractBytesInterceptor;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.util.Strings;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.LinkedList;

import static com.ak.appliance.purelogic.comm.bytes.PureLogicFrame.FRAME_LEN;

public final class PureLogicBytesInterceptor extends AbstractBytesInterceptor<PureLogicFrame, PureLogicFrame> {
  private final StringBuilder frame = new StringBuilder(FRAME_LEN);

  public PureLogicBytesInterceptor(String name) {
    super(name, BytesInterceptor.BaudRate.BR_115200, (FRAME_LEN / Character.BYTES) * Character.BYTES);
  }

  @Override
  protected Collection<PureLogicFrame> innerProcessIn(ByteBuffer src) {
    Collection<PureLogicFrame> responses = new LinkedList<>();
    while (src.hasRemaining()) {
      frame.append((char) src.get());
      if (frame.length() == FRAME_LEN) {
        PureLogicFrame.of(frame).ifPresentOrElse(pureLogicFrame -> {
          logSkippedBytes(true);
          responses.add(pureLogicFrame);
          frame.delete(0, frame.indexOf(Strings.NEW_LINE) + 1);
        }, () -> {
          logSkippedBytes(false);
          ignoreBuffer().putChar(frame.charAt(0));
          frame.deleteCharAt(0);
        });
      }
    }
    return responses;
  }
}
