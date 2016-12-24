package com.ak.comm.core;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.ak.comm.util.LogUtils;
import com.ak.logging.BinaryLogBuilder;

public final class SafeByteChannel implements WritableByteChannel {
  private static final SeekableByteChannel EMPTY_CHANNEL = new EmptyByteChannel();
  @Nonnull
  private final String namePrefix;
  @Nonnull
  private SeekableByteChannel channel = EMPTY_CHANNEL;
  @Nullable
  private Path path;
  private boolean initialized;

  public SafeByteChannel(@Nonnull String namePrefix) {
    this.namePrefix = namePrefix;
  }

  @Nullable
  Path getPath() {
    return path;
  }

  @Override
  public int write(@Nonnull ByteBuffer src) {
    if (!initialized) {
      try {
        path = new BinaryLogBuilder(namePrefix).build().getPath();
        channel = Files.newByteChannel(path, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
      }
      catch (IOException ex) {
        Logger.getLogger(getClass().getName()).log(Level.WARNING, namePrefix, ex);
      }
      finally {
        initialized = true;
      }
    }

    try {
      return channel.write(src);
    }
    catch (IOException e) {
      Logger.getLogger(getClass().getName()).log(LogUtils.LOG_LEVEL_ERRORS, e.getMessage(), e);
      return 0;
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
}
