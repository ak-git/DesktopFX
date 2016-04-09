package com.ak.comm.file;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ak.comm.core.AbstractService;

final class FileService extends AbstractService<ByteBuffer> {
  private final ExecutorService executor = Executors.newSingleThreadExecutor();
  private final Path file;

  FileService(Path file) {
    this.file = file;
  }

  void open() {
    if (Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS)) {
      executor.execute(() -> {
        try (SeekableByteChannel channel = Files.newByteChannel(file)) {
          Logger.getLogger(getClass().getName()).log(Level.INFO, String.format("Open file [ %s ]", file));
          ByteBuffer buffer = ByteBuffer.allocate(1024 * 4);
          while (channel.read(buffer) > 0) {
            buffer.flip();
            bufferPublish().onNext(buffer);
            buffer.clear();
            if (executor.isShutdown()) {
              break;
            }
          }
        }
        catch (ClosedChannelException e) {
          Logger.getLogger(getClass().getName()).log(Level.CONFIG, file.toString(), e);
          bufferPublish().onError(e);
        }
        catch (IOException e) {
          Logger.getLogger(getClass().getName()).log(Level.WARNING, file.toString(), e);
          bufferPublish().onError(e);
        }
      });
    }
    else {
      Logger.getLogger(getClass().getName()).log(Level.CONFIG, String.format("File [ %s ] is not a regular file", file));
    }
  }

  @Override
  public void close() {
    executor.shutdownNow();
    bufferPublish().onCompleted();
  }

  @Override
  public String toString() {
    return String.format("%s@%x{file = %s}", getClass().getSimpleName(), hashCode(), file);
  }
}
