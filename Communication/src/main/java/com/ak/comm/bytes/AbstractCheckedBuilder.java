package com.ak.comm.bytes;

import com.ak.util.Builder;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Logger;

import static com.ak.comm.bytes.LogUtils.LOG_LEVEL_ERRORS;

public abstract class AbstractCheckedBuilder<T> implements BytesChecker, Builder<T> {
  private final ByteBuffer buffer;

  protected AbstractCheckedBuilder(ByteBuffer buffer) {
    this.buffer = Objects.requireNonNull(buffer);
  }

  @Override
  public final void bufferLimit(ByteBuffer buffer) {
    throw new UnsupportedOperationException(Arrays.toString(buffer.array()));
  }

  public final ByteBuffer buffer() {
    return buffer;
  }

  protected final void logWarning() {
    Logger.getLogger(BufferFrame.class.getName()).log(LOG_LEVEL_ERRORS,
        () -> "Invalid response format: {%s}".formatted(Arrays.toString(buffer.array())));
  }
}
