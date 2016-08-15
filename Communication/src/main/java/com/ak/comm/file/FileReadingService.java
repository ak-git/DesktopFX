package com.ak.comm.file;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.ak.comm.core.AbstractService;
import rx.Observer;
import rx.Subscription;

final class FileReadingService extends AbstractService<ByteBuffer> {
  private static final int CAPACITY_4K = 1024 * 4;
  private final Path fileToRead;

  FileReadingService(@Nullable Path fileToRead, @Nonnull Observer<ByteBuffer> observer) {
    this.fileToRead = fileToRead;
    if (fileToRead != null) {
      if (Files.isRegularFile(fileToRead, LinkOption.NOFOLLOW_LINKS)) {
        Subscription subscription = getBufferObservable().subscribe(observer);
        try (ReadableByteChannel readableByteChannel = Files.newByteChannel(fileToRead, StandardOpenOption.READ)) {
          Logger.getLogger(getClass().getName()).log(Level.INFO, String.format("#%x Open file [ %s ]", hashCode(), fileToRead));
          ByteBuffer buffer = ByteBuffer.allocate(CAPACITY_4K);
          while (readableByteChannel.read(buffer) > 0) {
            buffer.flip();
            bufferPublish().onNext(buffer);
            buffer.clear();
          }
          bufferPublish().onCompleted();
          Logger.getLogger(getClass().getName()).log(Level.CONFIG, "Close file " + fileToRead);
        }
        catch (IOException e) {
          logErrorAndClose(Level.CONFIG, fileToRead.toString(), e);
        }
        catch (Exception e) {
          logErrorAndClose(Level.WARNING, fileToRead.toString(), e);
        }
        finally {
          subscription.unsubscribe();
        }
      }
      else {
        Logger.getLogger(getClass().getName()).log(Level.CONFIG, String.format("File [ %s ] is not a regular file", fileToRead));
      }
    }
  }

  @Override
  public String toString() {
    return String.format("%s@%x{file = %s}", getClass().getSimpleName(), hashCode(), fileToRead);
  }
}
