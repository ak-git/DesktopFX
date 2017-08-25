package com.ak.comm.core;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.ak.comm.util.LogUtils;

final class ConcurrentAsyncFileChannel implements Closeable {
  @Nonnull
  private final Callable<AsynchronousFileChannel> channelCallable;
  @Nonnull
  private final ReadWriteLock lock = new ReentrantReadWriteLock();
  @Nullable
  private AsynchronousFileChannel channel;
  @Nonnegative
  private long writePos;

  ConcurrentAsyncFileChannel(@Nonnull Callable<AsynchronousFileChannel> channelCallable) {
    this.channelCallable = channelCallable;
  }

  int write(@Nonnull ByteBuffer src) {
    int countBytes;
    lock.writeLock().lock();
    try {
      countBytes = operate(c -> c.write(src, writePos).get());
      if (countBytes > 0) {
        writePos += countBytes;
      }
    }
    finally {
      lock.writeLock().unlock();
    }
    return countBytes;
  }

  int read(@Nonnull ByteBuffer dst, @Nonnegative long position) {
    int countBytes;
    lock.readLock().lock();
    try {
      countBytes = operate(c -> c.read(dst, position).get());
    }
    finally {
      lock.readLock().unlock();
    }
    return countBytes;
  }

  @Override
  public void close() {
    lock.writeLock().lock();
    try {
      if (channel != null) {
        try {
          channel.close();
        }
        catch (IOException e) {
          Logger.getLogger(getClass().getName()).log(LogUtils.LOG_LEVEL_ERRORS, e.getMessage(), e);
        }
        finally {
          channel = null;
        }
      }
    }
    finally {
      lock.writeLock().unlock();
    }
  }

  private int operate(@Nonnull ChannelOperation operation) {
    int bytesCount = 0;
    try {
      if (channel == null) {
        channel = channelCallable.call();
      }
      if (channel != null) {
        bytesCount = operation.operate(channel);
      }
    }
    catch (InterruptedException e) {
      Logger.getLogger(getClass().getName()).log(LogUtils.LOG_LEVEL_ERRORS, e.getMessage(), e);
    }
    catch (Exception e) {
      Logger.getLogger(getClass().getName()).log(Level.WARNING, e.getMessage(), e);
      bytesCount = -1;
    }
    return bytesCount;
  }

  private interface ChannelOperation {
    int operate(@Nonnull AsynchronousFileChannel channel) throws InterruptedException, ExecutionException;
  }
}
