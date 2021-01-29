package com.ak.comm.interceptor.purelogic;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.LinkedList;

import javax.annotation.Nonnull;

import com.ak.comm.bytes.purelogic.PureLogicFrame;
import com.ak.comm.interceptor.AbstractBytesInterceptor;
import com.ak.comm.interceptor.BytesInterceptor;

import static com.ak.comm.bytes.purelogic.PureLogicFrame.FRAME_LEN;

public final class PureLogicBytesInterceptor extends AbstractBytesInterceptor<PureLogicFrame, PureLogicFrame> {
  private final StringBuilder frame = new StringBuilder(FRAME_LEN);

  public PureLogicBytesInterceptor() {
    super("PureLogic", BytesInterceptor.BaudRate.BR_115200, PureLogicFrame.StepCommand.MICRON_015.action(false), FRAME_LEN);
  }

  @Nonnull
  @Override
  protected Collection<PureLogicFrame> innerProcessIn(@Nonnull ByteBuffer src) {
    Collection<PureLogicFrame> responses = new LinkedList<>();
    while (src.hasRemaining()) {
      frame.append((char) src.get());
      if (frame.length() == FRAME_LEN) {
        PureLogicFrame pureLogicFrame = PureLogicFrame.of(frame);
        if (pureLogicFrame == null) {
          ignoreBuffer().putChar(frame.charAt(0));
          logSkippedBytes(false);
          frame.deleteCharAt(0);
        }
        else {
          logSkippedBytes(true);
          responses.add(pureLogicFrame);
          frame.delete(0, frame.length());
        }
      }
    }
    return responses;
  }
}
