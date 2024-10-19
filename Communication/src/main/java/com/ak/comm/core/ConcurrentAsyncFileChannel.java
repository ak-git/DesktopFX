package com.ak.comm.core;

import com.ak.comm.bytes.LogUtils;
import org.jspecify.annotations.Nullable;

import javax.annotation.Nonnegative;
import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ConcurrentAsyncFileChannel implements Closeable {
  private static final Logger LOGGER = Logger.getLogger(ConcurrentAsyncFileChannel.class.getName());
  private final Callable<Optional<AsynchronousFileChannel>> channelCallable;
  private final StampedLock lock = new StampedLock();
  private @Nullable AsynchronousFileChannel channel;
  @Nonnegative
  private long writePos;

  public ConcurrentAsyncFileChannel(Callable<Optional<AsynchronousFileChannel>> channelCallable) {
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
    Runnable operation = () -> {
      if (channel != null) {
        try {
          channel.close();
        }
        catch (IOException e) {
          LOGGER.log(LogUtils.LOG_LEVEL_ERRORS, e.getMessage(), e);
        }
        finally {
          channel = null;
          writePos = 0;
        }
      }
    };

    if (Thread.currentThread().isInterrupted()) {
      operation.run();
    }
    else {
      writeLock(operation);
    }
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

  private long operate(Function<AsynchronousFileChannel, Future<Integer>> operation) {
    long bytesCount = 0;
    try {
      if (channel == null) {
        channel = channelCallable.call().orElse(null);
      }
      if (channel != null) {
        bytesCount = operation.apply(channel).get();
      }
    }
    catch (InterruptedException e) {
      LOGGER.log(LogUtils.LOG_LEVEL_ERRORS, e.getMessage(), e);
      Thread.currentThread().interrupt();
    }
    catch (Exception e) {
      LOGGER.log(Level.WARNING, e.getMessage(), e);
      bytesCount = -1;
    }
    return bytesCount;
  }
}
