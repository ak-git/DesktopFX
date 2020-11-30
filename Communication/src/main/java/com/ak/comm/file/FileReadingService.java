package com.ak.comm.file;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.Flow;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.comm.converter.Converter;
import com.ak.comm.converter.Variable;
import com.ak.comm.core.AbstractConvertableService;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.logging.LogBuilders;

import static com.ak.util.LogUtils.LOG_LEVEL_ERRORS;

final class FileReadingService<T, R, V extends Enum<V> & Variable<V>>
    extends AbstractConvertableService<T, R, V> implements Flow.Subscription {
  private static final int CAPACITY_4K = 1024 * 4;
  private static final Lock LOCK = new ReentrantLock();
  @Nonnull
  private final Path fileToRead;
  @Nonnegative
  private long requestSamples = Long.MAX_VALUE;
  @Nonnull
  private Callable<AsynchronousFileChannel> convertedFileChannelProvider = () -> null;
  private volatile boolean disposed;

  FileReadingService(@Nonnull Path fileToRead, @Nonnull BytesInterceptor<T, R> bytesInterceptor,
                     @Nonnull Converter<R, V> responseConverter) {
    super(bytesInterceptor, responseConverter);
    Objects.requireNonNull(fileToRead);
    this.fileToRead = fileToRead;
  }

  @Override
  public void subscribe(Flow.Subscriber<? super int[]> s) {
    if (Files.isRegularFile(fileToRead, LinkOption.NOFOLLOW_LINKS) && Files.exists(fileToRead, LinkOption.NOFOLLOW_LINKS) &&
        Files.isReadable(fileToRead)) {
      s.onSubscribe(this);

      LOCK.lock();
      try (SeekableByteChannel seekableByteChannel = Files.newByteChannel(fileToRead, StandardOpenOption.READ)) {
        Logger.getLogger(getClass().getName()).log(Level.CONFIG, () -> "#%x Open file [ %s ]".formatted(hashCode(), fileToRead));

        MessageDigest md = MessageDigest.getInstance("SHA-512");
        if (isChannelProcessed(seekableByteChannel, md::update)) {
          String md5Code = digestToString(md.digest("2020.11.07".getBytes(Charset.defaultCharset())));
          Path convertedFile = LogBuilders.CONVERTER_FILE.build(md5Code).getPath();
          if (Files.exists(convertedFile, LinkOption.NOFOLLOW_LINKS)) {
            convertedFileChannelProvider = () -> AsynchronousFileChannel.open(convertedFile, StandardOpenOption.READ);
            Logger.getLogger(getClass().getName()).log(Level.INFO,
                () -> "#%x File [ %s ] with hash = [ %s ] is already processed".formatted(hashCode(), fileToRead, md5Code));
          }
          else {
            Logger.getLogger(getClass().getName()).log(Level.INFO,
                () -> "#%x Read file [ %s ], hash = [ %s ]".formatted(hashCode(), fileToRead, md5Code));
            Path tempConverterFile = LogBuilders.CONVERTER_FILE.build("temp." + md5Code).getPath();
            convertedFileChannelProvider = () -> AsynchronousFileChannel.open(tempConverterFile,
                StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.TRUNCATE_EXISTING);

            boolean processed = isChannelProcessed(seekableByteChannel, new Consumer<>() {
              @Nonnegative
              private long samplesCounter;

              @Override
              public void accept(@Nonnull ByteBuffer byteBuffer) {
                logBytes(byteBuffer);
                process(byteBuffer, ints -> {
                  if (samplesCounter < requestSamples) {
                    s.onNext(ints);
                  }
                  samplesCounter++;
                });
              }
            });

            if (processed && Files.exists(tempConverterFile)) {
              Files.copy(tempConverterFile, convertedFile, LinkOption.NOFOLLOW_LINKS, StandardCopyOption.REPLACE_EXISTING);
              tempConverterFile.toFile().deleteOnExit();
            }
          }
        }

        if (!disposed) {
          s.onComplete();
        }
      }
      catch (ClosedByInterruptException e) {
        Logger.getLogger(getClass().getName()).log(Level.CONFIG, fileToRead.toString(), e);
      }
      catch (Exception e) {
        Logger.getLogger(getClass().getName()).log(Level.WARNING, fileToRead.toString(), e);
        s.onError(e);
      }
      finally {
        LOCK.unlock();
        Logger.getLogger(getClass().getName()).log(Level.INFO, () -> "Close file " + fileToRead);
      }
    }
    else {
      Logger.getLogger(getClass().getName()).log(LOG_LEVEL_ERRORS, () -> "File [ %s ] is not a regular file".formatted(fileToRead));
    }
  }

  @Override
  public String toString() {
    return "%s@%x{file = %s}".formatted(getClass().getSimpleName(), hashCode(), fileToRead);
  }

  @Override
  public void request(@Nonnegative long requestSamples) {
    this.requestSamples = requestSamples;
  }

  @Override
  public void cancel() {
    close();
  }

  @Override
  public void close() {
    try {
      disposed = true;
    }
    finally {
      super.close();
    }
  }

  @Override
  public AsynchronousFileChannel call() throws Exception {
    return convertedFileChannelProvider.call();
  }

  private boolean isChannelProcessed(@Nonnull SeekableByteChannel seekableByteChannel, @Nonnull Consumer<ByteBuffer> consumer) throws IOException {
    ByteBuffer buffer = ByteBuffer.allocate(CAPACITY_4K);
    boolean readFlag = false;
    seekableByteChannel.position(0);
    while (seekableByteChannel.read(buffer) > 0 && !disposed) {
      buffer.flip();
      consumer.accept(buffer);
      buffer.clear();
      readFlag = true;
    }
    return readFlag && !disposed;
  }

  private static String digestToString(@Nonnull byte[] digest) {
    return IntStream.range(0, digest.length).filter(value -> value % 4 == 0)
        .mapToObj(i -> "%02x".formatted(digest[i])).collect(Collectors.joining());
  }
}
