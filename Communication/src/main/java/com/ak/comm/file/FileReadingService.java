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

import com.ak.comm.core.AbstractService;
import org.reactivestreams.Subscriber;

import static com.ak.comm.core.LogLevels.LOG_LEVEL_ERRORS;

public final class FileReadingService extends AbstractService<ByteBuffer> {
  private static final int CAPACITY_4K = 1024 * 4;
  @Nonnull
  private final Path fileToRead;
  private volatile boolean canceled;

  FileReadingService(@Nonnull Path fileToRead) {
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
          logBytes(buffer);
          s.onNext(buffer);
          buffer.clear();
        }
        s.onComplete();
        Logger.getLogger(getClass().getName()).log(Level.INFO, "Close file " + fileToRead);
      }
      catch (Exception e) {
        Logger.getLogger(getClass().getName()).log(LOG_LEVEL_ERRORS, fileToRead.toString(), e);
        s.onError(e);
      }
    }
    else {
      Logger.getLogger(getClass().getName()).log(LOG_LEVEL_ERRORS, String.format("File [ %s ] is not a regular file", fileToRead));
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
