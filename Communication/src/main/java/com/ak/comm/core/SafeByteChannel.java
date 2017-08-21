package com.ak.comm.core;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

import com.ak.comm.util.LogUtils;

public final class SafeByteChannel implements ByteChannel {
  public static final SeekableByteChannel EMPTY_CHANNEL = new EmptyByteChannel();
  @Nonnull
  private final Callable<SeekableByteChannel> channelProvider;
  @Nonnull
  private SeekableByteChannel channel = EMPTY_CHANNEL;
  private boolean initialized;

  public SafeByteChannel(@Nonnull Callable<SeekableByteChannel> channelProvider) {
    this.channelProvider = channelProvider;
  }

  @Override
  public int write(@Nonnull ByteBuffer src) {
    initialize();
    try {
      return channel.write(src);
    }
    catch (IOException e) {
      Logger.getLogger(getClass().getName()).log(LogUtils.LOG_LEVEL_ERRORS, e.getMessage(), e);
      return -1;
    }
  }

  @Override
  public boolean isOpen() {
    return channel.isOpen();
  }

  @Override
  public void close() {
    try {
      channel.close();
    }
    catch (IOException e) {
      Logger.getLogger(getClass().getName()).log(LogUtils.LOG_LEVEL_ERRORS, e.getMessage(), e);
    }
    finally {
      initialized = false;
    }
  }

  @Override
  public int read(ByteBuffer dst) {
    initialize();
    try {
      return channel.read(dst);
    }
    catch (IOException e) {
      Logger.getLogger(getClass().getName()).log(LogUtils.LOG_LEVEL_ERRORS, e.getMessage(), e);
      return -1;
    }
  }

  private void initialize() {
    if (!initialized) {
      try {
        channel = channelProvider.call();
      }
      catch (Exception ex) {
        Logger.getLogger(getClass().getName()).log(Level.WARNING, channelProvider.toString(), ex);
      }
      finally {
        initialized = true;
      }
    }
  }
}
