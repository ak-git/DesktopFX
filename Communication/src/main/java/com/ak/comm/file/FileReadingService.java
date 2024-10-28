package com.ak.comm.file;

import com.ak.comm.converter.Converter;
import com.ak.comm.converter.Variable;
import com.ak.comm.core.AbstractConvertableService;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.logging.LogBuilders;
import com.ak.util.Strings;

import javax.annotation.Nonnegative;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.Flow;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.ak.comm.bytes.LogUtils.LOG_LEVEL_ERRORS;
import static java.nio.file.StandardOpenOption.*;

final class FileReadingService<T, R, V extends Enum<V> & Variable<V>>
    extends AbstractConvertableService<T, R, V> implements Flow.Subscription {
  private final Path fileToRead;
  @Nonnegative
  private long requestSamples = Long.MAX_VALUE;
  private Callable<Optional<AsynchronousFileChannel>> convertedFileChannelProvider = Optional::empty;
  private volatile boolean disposed;

  FileReadingService(Path fileToRead, BytesInterceptor<T, R> bytesInterceptor, Converter<R, V> responseConverter) {
    super(bytesInterceptor, responseConverter);
    Objects.requireNonNull(fileToRead);
    this.fileToRead = fileToRead;
  }

  @Override
  public void subscribe(Flow.Subscriber<? super int[]> s) {
    if (Files.isReadable(fileToRead)) {
      s.onSubscribe(this);

      try (var seekableByteChannel = Files.newByteChannel(fileToRead, READ)) {
        checkThenOpen(s.toString(), seekableByteChannel, new Consumer<>() {
          @Nonnegative
          private long samplesCounter;

          @Override
          public void accept(ByteBuffer byteBuffer) {
            logBytes(byteBuffer);
            process(byteBuffer, ints -> {
              if (samplesCounter < requestSamples) {
                s.onNext(ints);
              }
              samplesCounter++;
            });
          }
        });
        if (!disposed) {
          s.onComplete();
        }
      }
      catch (ClosedByInterruptException e) {
        Logger.getLogger(getClass().getName()).log(Level.CONFIG, fileToRead.toString(), e);
        Thread.currentThread().interrupt();
      }
      catch (Exception e) {
        Logger.getLogger(getClass().getName()).log(Level.WARNING, fileToRead.toString(), e);
        s.onError(e);
      }
      finally {
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
  public Optional<AsynchronousFileChannel> call() throws Exception {
    return convertedFileChannelProvider.call();
  }

  private void checkThenOpen(String md5Base, SeekableByteChannel seekableByteChannel, Consumer<ByteBuffer> doOnOpen)
      throws NoSuchAlgorithmException, IOException {
    Logger.getLogger(getClass().getName()).log(Level.CONFIG, () -> "#%08x Open file [ %s ]".formatted(hashCode(), fileToRead));
    var md = MessageDigest.getInstance("SHA-512");
    if (isChannelProcessed(seekableByteChannel, md::update)) {
      var md5Code = digestToString(md.digest(md5Base.getBytes(Charset.defaultCharset())));
      var convertedFile = LogBuilders.CONVERTER_FILE.build(md5Code).getPath();
      if (Files.exists(convertedFile, LinkOption.NOFOLLOW_LINKS)) {
        convertedFileChannelProvider = () -> Optional.of(AsynchronousFileChannel.open(convertedFile, READ));
        Logger.getLogger(getClass().getName()).log(Level.INFO,
            () -> "#%08x File [ %s ] with hash = [ %s ] is already processed".formatted(hashCode(), fileToRead, md5Code));
      }
      else {
        Logger.getLogger(getClass().getName()).log(Level.INFO,
            () -> "#%08x Read file [ %s ], hash = [ %s ]".formatted(hashCode(), fileToRead, md5Code));
        var tempConverterFile = LogBuilders.CONVERTER_FILE.build("temp." + md5Code).getPath();
        convertedFileChannelProvider = () -> Optional.of(
            AsynchronousFileChannel.open(tempConverterFile, CREATE, WRITE, READ, TRUNCATE_EXISTING)
        );

        boolean processed = isChannelProcessed(seekableByteChannel, doOnOpen);
        if (processed && Files.exists(tempConverterFile)) {
          Files.copy(tempConverterFile, convertedFile, LinkOption.NOFOLLOW_LINKS, StandardCopyOption.REPLACE_EXISTING);
          tempConverterFile.toFile().deleteOnExit();
        }
      }
    }
  }

  private boolean isChannelProcessed(SeekableByteChannel seekableByteChannel,
                                     Consumer<ByteBuffer> consumer) throws IOException {
    int blockSize = (int) Files.getFileStore(Paths.get(Strings.EMPTY)).getBlockSize();
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

  private static String digestToString(byte[] digest) {
    String s = HexFormat.of().formatHex(digest);
    return IntStream.range(0, s.length()).filter(value -> value % 4 == 0)
        .mapToObj(i -> s.subSequence(i, i + 1)).collect(Collectors.joining());
  }
}
