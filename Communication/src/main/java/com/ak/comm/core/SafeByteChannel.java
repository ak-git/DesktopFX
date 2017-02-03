package com.ak.comm.core;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

import com.ak.comm.util.LogUtils;
import com.ak.logging.BinaryLogBuilder;

public final class SafeByteChannel implements ByteChannel {
  private static final SeekableByteChannel EMPTY_CHANNEL = new EmptyByteChannel();
  @Nonnull
  private final String namePrefix;
  @Nonnull
  private SeekableByteChannel channel = EMPTY_CHANNEL;
  private boolean initialized;

  public SafeByteChannel(@Nonnull Class<?> aClass) {
    namePrefix = aClass.getSimpleName();
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
        Path path = new BinaryLogBuilder().fileNameWithTime(namePrefix).build().getPath();
        channel = Files.newByteChannel(path, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE, StandardOpenOption.READ);
      }
      catch (IOException ex) {
        Logger.getLogger(getClass().getName()).log(Level.WARNING, namePrefix, ex);
      }
      finally {
        initialized = true;
      }
    }
  }
}
