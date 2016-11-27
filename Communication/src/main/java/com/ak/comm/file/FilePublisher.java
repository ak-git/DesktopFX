package com.ak.comm.file;

import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

import com.ak.comm.bytes.AbstractBufferFrame;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public final class FilePublisher implements Publisher<ByteBuffer>, Subscription {
  private static final Level LOG_LEVEL_BYTES = Level.FINEST;
  private static final int CAPACITY_4K = 1024 * 4;
  private final Logger logger = Logger.getLogger(getClass().getName());
  @Nonnull
  private final Path fileToRead;
  private volatile boolean canceled;

  public FilePublisher(@Nonnull Path fileToRead) {
    Objects.requireNonNull(fileToRead);
    this.fileToRead = fileToRead;
  }

  @Override
  public void subscribe(@Nonnull Subscriber<? super ByteBuffer> s) {
    if (Files.isRegularFile(fileToRead, LinkOption.NOFOLLOW_LINKS)) {
      s.onSubscribe(this);
      try (ReadableByteChannel readableByteChannel = Files.newByteChannel(fileToRead, StandardOpenOption.READ)) {
        Logger.getLogger(getClass().getName()).log(Level.INFO, String.format("#%x Open file [ %s ]", hashCode(), fileToRead));
        ByteBuffer buffer = ByteBuffer.allocate(CAPACITY_4K);
        while (readableByteChannel.read(buffer) > 0 && !canceled) {
          buffer.flip();
          if (logger.isLoggable(LOG_LEVEL_BYTES)) {
            logger.log(LOG_LEVEL_BYTES, String.format("#%x %s IN from hardware", hashCode(), AbstractBufferFrame.toString(getClass(), buffer)));
          }
          s.onNext(buffer);
          buffer.clear();
        }
        s.onComplete();
        Logger.getLogger(getClass().getName()).log(Level.INFO, "Close file " + fileToRead);
      }
      catch (Exception e) {
        Logger.getLogger(getClass().getName()).log(Level.CONFIG, fileToRead.toString(), e);
        s.onError(e);
      }
    }
    else {
      Logger.getLogger(getClass().getName()).log(Level.CONFIG, String.format("File [ %s ] is not a regular file", fileToRead));
    }
  }

  @Override
  public String toString() {
    return String.format("%s@%x{file = %s}", getClass().getSimpleName(), hashCode(), fileToRead);
  }

  @Override
  public void request(long n) {
  }

  @Override
  public void cancel() {
    canceled = true;
  }
}
