package com.ak.comm.file;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

import com.ak.comm.converter.Converter;
import com.ak.comm.converter.Variable;
import com.ak.comm.core.AbstractConvertableService;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.comm.logging.LogBuilders;
import io.reactivex.disposables.Disposable;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import static com.ak.comm.util.LogUtils.LOG_LEVEL_ERRORS;

final class FileReadingService<RESPONSE, REQUEST, EV extends Enum<EV> & Variable<EV>>
    extends AbstractConvertableService<RESPONSE, REQUEST, EV> implements Disposable, Subscription {
  private static final int CAPACITY_4K = 1024 * 4;
  private static final Lock LOCK = new ReentrantLock();
  @Nonnull
  private final Path fileToRead;
  @Nonnull
  private volatile Callable<AsynchronousFileChannel> convertedFileChannelProvider = () -> null;
  private volatile boolean disposed;

  FileReadingService(@Nonnull Path fileToRead, @Nonnull BytesInterceptor<RESPONSE, REQUEST> bytesInterceptor,
                     @Nonnull Converter<RESPONSE, EV> responseConverter) {
    super(bytesInterceptor, responseConverter);
    Objects.requireNonNull(fileToRead);
    this.fileToRead = fileToRead;
  }

  @Override
  public void subscribe(Subscriber<? super int[]> s) {
    if (Files.isRegularFile(fileToRead, LinkOption.NOFOLLOW_LINKS) && Files.exists(fileToRead, LinkOption.NOFOLLOW_LINKS) &&
        Files.isReadable(fileToRead)) {
      s.onSubscribe(this);

      LOCK.lock();
      try (SeekableByteChannel seekableByteChannel = Files.newByteChannel(fileToRead, StandardOpenOption.READ)) {
        Logger.getLogger(getClass().getName()).log(Level.CONFIG, String.format("#%x Open file [ %s ]", hashCode(), fileToRead));

        MessageDigest md5 = MessageDigest.getInstance("MD5");
        if (isChannelProcessed(seekableByteChannel, md5::update)) {
          String md5Code = digestToString(md5);
          Path convertedFile = LogBuilders.CONVERTER_FILE.build(md5Code).getPath();
          if (Files.exists(convertedFile, LinkOption.NOFOLLOW_LINKS)) {
            convertedFileChannelProvider = () -> AsynchronousFileChannel.open(convertedFile, StandardOpenOption.READ);
            Logger.getLogger(getClass().getName()).log(Level.INFO,
                String.format("#%x File [ %s ] with MD5 = [ %s ] is already processed", hashCode(), fileToRead, md5Code));
          }
          else {
            Logger.getLogger(getClass().getName()).log(Level.INFO,
                String.format("#%x Read file [ %s ], MD5 = [ %s ]", hashCode(), fileToRead, md5Code));
            Path tempConverterFile = LogBuilders.CONVERTER_FILE.build("tempConverterFile" + md5Code).getPath();
            convertedFileChannelProvider = () -> AsynchronousFileChannel.open(tempConverterFile,
                StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.TRUNCATE_EXISTING);

            boolean processed = isChannelProcessed(seekableByteChannel, byteBuffer -> {
              logBytes(byteBuffer);
              process(byteBuffer).forEach(s::onNext);
            });

            if (processed && Files.exists(tempConverterFile)) {
              Files.copy(tempConverterFile, convertedFile, LinkOption.NOFOLLOW_LINKS, StandardCopyOption.REPLACE_EXISTING);
              tempConverterFile.toFile().deleteOnExit();
            }
          }
        }

        if (!isDisposed()) {
          s.onComplete();
        }
      }
      catch (ClosedByInterruptException e) {
        Logger.getLogger(getClass().getName()).log(Level.CONFIG, fileToRead.toString(), e);
      }
      catch (IOException | NoSuchAlgorithmException e) {
        Logger.getLogger(getClass().getName()).log(Level.WARNING, fileToRead.toString(), e);
        s.onError(e);
      }
      finally {
        LOCK.unlock();
        Logger.getLogger(getClass().getName()).log(Level.INFO, "Close file " + fileToRead);
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
    try {
      close();
    }
    catch (Exception e) {
      Logger.getLogger(getClass().getName()).log(LOG_LEVEL_ERRORS, e.getMessage(), e);
    }
  }

  @Override
  public void dispose() {
    disposed = true;
  }

  @Override
  public boolean isDisposed() {
    return disposed;
  }

  @Override
  public void close() throws IOException {
    try {
      dispose();
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
    while (seekableByteChannel.read(buffer) > 0 && !isDisposed()) {
      buffer.flip();
      consumer.accept(buffer);
      buffer.clear();
      readFlag = true;
    }
    return readFlag && !isDisposed();
  }

  private static String digestToString(@Nonnull MessageDigest messageDigest) {
    byte[] digest = messageDigest.digest();
    StringBuilder sb = new StringBuilder(digest.length * 2);
    for (byte b : digest) {
      sb.append(String.format("%x", b));
    }
    return sb.toString();
  }
}
