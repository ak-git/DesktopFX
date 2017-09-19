package com.ak.comm.core;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.ak.comm.util.LogUtils;

public final class ConcurrentAsyncFileChannel implements Closeable {
  @Nonnull
  private final Callable<AsynchronousFileChannel> channelCallable;
  @Nonnull
  private final ReadWriteLock lock = new ReentrantReadWriteLock();
  @Nullable
  private AsynchronousFileChannel channel;
  @Nonnegative
  private long writePos;

  public ConcurrentAsyncFileChannel(@Nonnull Callable<AsynchronousFileChannel> channelCallable) {
    this.channelCallable = channelCallable;
  }

  public void write(@Nonnull ByteBuffer src) {
    lock.writeLock().lock();
    try {
      long countBytes = operate(c -> c.write(src, writePos));
      if (countBytes > 0) {
        writePos += countBytes;
      }
    }
    finally {
      lock.writeLock().unlock();
    }
  }

  void read(@Nonnull ByteBuffer dst, @Nonnegative long position) {
    lock.readLock().lock();
    try {
      operate(c -> c.read(dst, position));
    }
    finally {
      lock.readLock().unlock();
    }
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
          writePos = 0;
        }
      }
    }
    finally {
      lock.writeLock().unlock();
    }
  }

  private long operate(@Nonnull ChannelOperation operation) {
    long bytesCount = 0;
    try {
      if (channel == null) {
        channel = channelCallable.call();
      }
      if (channel != null) {
        bytesCount = operation.operate(channel).get();
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
    Future<Integer> operate(@Nonnull AsynchronousFileChannel channel) throws InterruptedException, ExecutionException, IOException;
  }
}
