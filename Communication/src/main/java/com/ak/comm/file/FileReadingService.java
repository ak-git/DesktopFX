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
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.comm.converter.Converter;
import com.ak.comm.converter.Variable;
import com.ak.comm.core.AbstractConvertableService;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.logging.LogBuilders;

import static com.ak.comm.bytes.LogUtils.LOG_LEVEL_ERRORS;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

final class FileReadingService<T, R, V extends Enum<V> & Variable<V>>
    extends AbstractConvertableService<T, R, V> implements Flow.Subscription {
  private static final Lock LOCK = new ReentrantLock();
  @Nonnull
  private final Path fileToRead;
  @Nonnegative
  private long requestSamples = Long.MAX_VALUE;
  @Nonnull
  private Callable<AsynchronousFileChannel> convertedFileChannelProvider = () -> null;
  private volatile boolean disposed;

  @ParametersAreNonnullByDefault
  FileReadingService(Path fileToRead, BytesInterceptor<T, R> bytesInterceptor, Converter<R, V> responseConverter) {
    super(bytesInterceptor, responseConverter);
    Objects.requireNonNull(fileToRead);
    this.fileToRead = fileToRead;
  }

  @Override
  public void subscribe(@Nonnull Flow.Subscriber<? super int[]> s) {
    if (Files.isRegularFile(fileToRead, LinkOption.NOFOLLOW_LINKS) && Files.exists(fileToRead, LinkOption.NOFOLLOW_LINKS) &&
        Files.isReadable(fileToRead)) {
      s.onSubscribe(this);

      LOCK.lock();
      try (var seekableByteChannel = Files.newByteChannel(fileToRead, READ)) {
        Logger.getLogger(getClass().getName()).log(Level.CONFIG, () -> "#%08x Open file [ %s ]".formatted(hashCode(), fileToRead));
        int blockSize = (int) Files.getFileStore(fileToRead).getBlockSize();
        var md = MessageDigest.getInstance("SHA-512");
        if (isChannelProcessed(blockSize, seekableByteChannel, md::update)) {
          var md5Code = digestToString(md.digest("2021.08.11".getBytes(Charset.defaultCharset())));
          var convertedFile = LogBuilders.CONVERTER_FILE.build(md5Code).getPath();
          if (Files.exists(convertedFile, LinkOption.NOFOLLOW_LINKS)) {
            convertedFileChannelProvider = () -> AsynchronousFileChannel.open(convertedFile, READ);
            Logger.getLogger(getClass().getName()).log(Level.INFO,
                () -> "#%08x File [ %s ] with hash = [ %s ] is already processed".formatted(hashCode(), fileToRead, md5Code));
          }
          else {
            Logger.getLogger(getClass().getName()).log(Level.INFO,
                () -> "#%08x Read file [ %s ], hash = [ %s ]".formatted(hashCode(), fileToRead, md5Code));
            var tempConverterFile = LogBuilders.CONVERTER_FILE.build("temp." + md5Code).getPath();
            convertedFileChannelProvider = () -> AsynchronousFileChannel.open(tempConverterFile,
                CREATE, WRITE, READ, TRUNCATE_EXISTING);

            boolean processed = isChannelProcessed(blockSize, seekableByteChannel, new Consumer<>() {
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
    return "%s@%08x{file = %s}".formatted(getClass().getSimpleName(), hashCode(), fileToRead);
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

  private boolean isChannelProcessed(@Nonnegative int blockSize, @Nonnull SeekableByteChannel seekableByteChannel,
                                     @Nonnull Consumer<ByteBuffer> consumer) throws IOException {
    var buffer = ByteBuffer.allocate(blockSize);
    var readFlag = false;
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
