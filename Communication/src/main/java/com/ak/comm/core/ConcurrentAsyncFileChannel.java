package com.ak.comm.core;

import com.ak.comm.bytes.LogUtils;
import org.jspecify.annotations.Nullable;

import javax.annotation.Nonnegative;
import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.locks.StampedLock;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ConcurrentAsyncFileChannel implements Closeable {
  private final Callable<AsynchronousFileChannel> channelCallable;
  private final StampedLock lock = new StampedLock();
  private @Nullable AsynchronousFileChannel channel;
  @Nonnegative
  private long writePos;

  public ConcurrentAsyncFileChannel(Callable<AsynchronousFileChannel> channelCallable) {
    this.channelCallable = channelCallable;
  }

  public void write(ByteBuffer src) {
    writeLock(() -> {
      long countBytes = operate(c -> c.write(src, writePos));
      if (countBytes > 0) {
        writePos += countBytes;
      }
    });
  }

  void read(ByteBuffer dst, @Nonnegative long position) {
    long stamp = lock.tryOptimisticRead();
    try {
      while (!Thread.currentThread().isInterrupted()) {
        if (stamp != 0L && lock.validate(stamp)) {
          operate(c -> c.read(dst, position));
          break;
        }
        stamp = lock.readLock();
      }
    }
    finally {
      if (StampedLock.isReadLockStamp(stamp)) {
        lock.unlockRead(stamp);
      }
    }
  }

  @Override
  public void close() {
    writeLock(() -> {
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
    });
  }

  private void writeLock(Runnable operation) {
    long stamp = lock.readLock();
    try {
      while (!Thread.currentThread().isInterrupted()) {
        long ws = lock.tryConvertToWriteLock(stamp);
        if (ws == 0L) {
          lock.unlockRead(stamp);
          stamp = lock.writeLock();
        }
        else {
          stamp = ws;
          operation.run();
          break;
        }
      }
    }
    finally {
      lock.unlock(stamp);
    }
  }

  private long operate(ChannelOperation operation) {
    long bytesCount = 0;
    try {
      if (channel == null) {
        channel = channelCallable.call();
      }
      bytesCount = operation.operate(channel).get();
    }
    catch (InterruptedException e) {
      Logger.getLogger(getClass().getName()).log(LogUtils.LOG_LEVEL_ERRORS, e.getMessage(), e);
      Thread.currentThread().interrupt();
    }
    catch (Exception e) {
      Logger.getLogger(getClass().getName()).log(Level.WARNING, e.getMessage(), e);
      bytesCount = -1;
    }
    return bytesCount;
  }

  private interface ChannelOperation {
    Future<Integer> operate(AsynchronousFileChannel channel);
  }
}
