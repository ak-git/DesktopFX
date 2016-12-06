package com.ak.logging;

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

public final class SafeByteChannel implements WritableByteChannel {
  private static final SeekableByteChannel EMPTY_CHANNEL = new SeekableByteChannel() {
    @Override
    public int read(ByteBuffer dst) {
      return 0;
    }

    @Override
    public int write(ByteBuffer src) {
      return 0;
    }

    @Override
    public long position() {
      return 0;
    }

    @Override
    public SeekableByteChannel position(long newPosition) {
      return this;
    }

    @Override
    public long size() {
      return 0;
    }

    @Override
    public SeekableByteChannel truncate(long size) {
      return this;
    }

    @Override
    public boolean isOpen() {
      return false;
    }

    @Override
    public void close() {
    }
  };
  @Nonnull
  private final String namePrefix;
  @Nonnull
  private SeekableByteChannel channel = EMPTY_CHANNEL;
  private boolean initialized;

  public SafeByteChannel(@Nonnull String namePrefix) {
    this.namePrefix = namePrefix;
  }

  @Override
  public int write(@Nonnull ByteBuffer src) {
    if (!initialized) {
      try {
        Path path = new BinaryLogBuilder(namePrefix).build().getPath();
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
      src.rewind();
      return channel.write(src);
    }
    catch (IOException e) {
      Logger.getLogger(getClass().getName()).log(Level.CONFIG, e.getMessage(), e);
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
      Logger.getLogger(getClass().getName()).log(Level.CONFIG, e.getMessage(), e);
    }
    finally {
      initialized = false;
    }
  }
}
