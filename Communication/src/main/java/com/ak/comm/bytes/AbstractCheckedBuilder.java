package com.ak.comm.bytes;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

import com.ak.util.Builder;

import static com.ak.util.LogUtils.LOG_LEVEL_ERRORS;

public abstract class AbstractCheckedBuilder<T> implements BytesChecker, Builder<T> {
  @Nonnull
  private final ByteBuffer buffer;

  protected AbstractCheckedBuilder(@Nonnull ByteBuffer buffer) {
    this.buffer = buffer;
  }

  @Override
  public final void bufferLimit(@Nonnull ByteBuffer buffer) {
    throw new UnsupportedOperationException(Arrays.toString(buffer.array()));
  }

  public final ByteBuffer buffer() {
    return buffer;
  }

  protected final void logWarning() {
    Logger.getLogger(BufferFrame.class.getName()).log(LOG_LEVEL_ERRORS,
        () -> String.format("Invalid response format: {%s}", Arrays.toString(buffer.array())));
  }
}
