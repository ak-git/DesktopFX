package com.ak.comm.bytes;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

import javafx.util.Builder;

public abstract class AbstractCheckedBuilder<T> implements BytesChecker, Builder<T> {
  @Nonnull
  private final ByteBuffer buffer;

  public AbstractCheckedBuilder(@Nonnull ByteBuffer buffer) {
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
    Logger.getLogger(AbstractBufferFrame.class.getName()).log(Level.CONFIG,
        String.format("Invalid response format: {%s}", Arrays.toString(buffer.array())));
  }
}
