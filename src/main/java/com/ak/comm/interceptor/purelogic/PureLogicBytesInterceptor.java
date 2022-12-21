package com.ak.comm.interceptor.purelogic;

import com.ak.comm.bytes.purelogic.PureLogicFrame;
import com.ak.comm.interceptor.AbstractBytesInterceptor;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.util.Strings;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.LinkedList;

import static com.ak.comm.bytes.purelogic.PureLogicFrame.FRAME_LEN;

@Component
@Profile("purelogic")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public final class PureLogicBytesInterceptor extends AbstractBytesInterceptor<PureLogicFrame, PureLogicFrame> {
  private final StringBuilder frame = new StringBuilder(FRAME_LEN);

  public PureLogicBytesInterceptor() {
    super("PureLogic", BytesInterceptor.BaudRate.BR_115200, null, FRAME_LEN);
  }

  @Nonnull
  @Override
  protected Collection<PureLogicFrame> innerProcessIn(@Nonnull ByteBuffer src) {
    Collection<PureLogicFrame> responses = new LinkedList<>();
    while (src.hasRemaining()) {
      frame.append((char) src.get());
      if (frame.length() == FRAME_LEN) {
        var pureLogicFrame = PureLogicFrame.of(frame);
        if (pureLogicFrame == null) {
          ignoreBuffer().putChar(frame.charAt(0));
          logSkippedBytes(false);
          frame.deleteCharAt(0);
        }
        else {
          logSkippedBytes(true);
          responses.add(pureLogicFrame);
          frame.delete(0, frame.indexOf(Strings.NEW_LINE) + 1);
        }
      }
    }
    return responses;
  }
}
