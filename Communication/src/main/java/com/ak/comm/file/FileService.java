package com.ak.comm.file;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ak.comm.core.AbstractService;
import rx.Observer;
import rx.Subscription;

final class FileService extends AbstractService<ByteBuffer> {
  private final Path file;
  private ByteChannel channel;

  FileService(Path file, Observer<ByteBuffer> observer) {
    this.file = file;
    if (Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS)) {
      Subscription subscription = getBufferObservable().subscribe(observer);
      try {
        channel = Files.newByteChannel(file);
        Logger.getLogger(getClass().getName()).log(Level.INFO, String.format("#%x Open file [ %s ]", hashCode(), file));
        ByteBuffer buffer = ByteBuffer.allocate(1024 * 4);
        while (channel.read(buffer) > 0) {
          buffer.flip();
          bufferPublish().onNext(buffer);
          buffer.clear();
        }
        bufferPublish().onCompleted();
        Logger.getLogger(getClass().getName()).log(Level.CONFIG, "Close file " + file);
      }
      catch (IOException e) {
        logErrorAndClose(Level.CONFIG, file.toString(), e);
      }
      catch (Exception e) {
        logErrorAndClose(Level.WARNING, file.toString(), e);
      }
      finally {
        subscription.unsubscribe();
      }
    }
    else {
      Logger.getLogger(getClass().getName()).log(Level.CONFIG, String.format("File [ %s ] is not a regular file", file));
    }
  }

  @Override
  public void close() {
    try {
      if (channel != null) {
        try {
          channel.close();
        }
        catch (IOException ex) {
          Logger.getLogger(getClass().getName()).log(Level.CONFIG, file.toString(), ex);
        }
      }
    }
    finally {
      bufferPublish().onCompleted();
    }
  }

  @Override
  public String toString() {
    return String.format("%s@%x{file = %s}", getClass().getSimpleName(), hashCode(), file);
  }
}
