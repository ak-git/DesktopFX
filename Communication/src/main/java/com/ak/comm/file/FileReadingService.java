package com.ak.comm.file;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

import com.ak.comm.converter.Converter;
import com.ak.comm.converter.Variable;
import com.ak.comm.core.AbstractConvertableService;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.util.Strings;
import io.reactivex.disposables.Disposable;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.springframework.util.DigestUtils;

import static com.ak.comm.util.LogUtils.LOG_LEVEL_ERRORS;

public final class FileReadingService<RESPONSE, REQUEST, EV extends Enum<EV> & Variable>
    extends AbstractConvertableService<RESPONSE, REQUEST, EV> implements Publisher<int[]>, Disposable {
  private static final int CAPACITY_4K = 1024 * 4;
  @Nonnull
  private final Path fileToRead;
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
      String md5Code = Strings.EMPTY;
      try (InputStream in = Files.newInputStream(fileToRead, StandardOpenOption.READ)) {
        md5Code = DigestUtils.appendMd5DigestAsHex(in, new StringBuilder()).toString();
      }
      catch (IOException ex) {
        Logger.getLogger(getClass().getName()).log(Level.WARNING,
            String.format("#%x Cannot calculate MD5 hash for file [ %s ]", hashCode(), fileToRead));
      }

      s.onSubscribe(this);
      try (ReadableByteChannel readableByteChannel = Files.newByteChannel(fileToRead, StandardOpenOption.READ)) {
        Logger.getLogger(getClass().getName()).log(Level.INFO, String.format("#%x Open file [ %s ], MD5 = [ %s ]", hashCode(), fileToRead, md5Code));
        ByteBuffer buffer = ByteBuffer.allocate(CAPACITY_4K);
        while (readableByteChannel.read(buffer) > 0 && !isDisposed()) {
          buffer.flip();
          logBytes(buffer);
          process(buffer).forEach(s::onNext);
          buffer.clear();
        }
        if (!isDisposed()) {
          s.onComplete();
        }
        Logger.getLogger(getClass().getName()).log(Level.INFO, "Close file " + fileToRead);
      }
      catch (ClosedByInterruptException e) {
        Logger.getLogger(getClass().getName()).log(Level.CONFIG, fileToRead.toString(), e);
        s.onError(e);
      }
      catch (Exception e) {
        Logger.getLogger(getClass().getName()).log(Level.WARNING, fileToRead.toString(), e);
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
  public void dispose() {
    disposed = true;
  }

  @Override
  public boolean isDisposed() {
    return disposed;
  }

  @Override
  public void close() {
    try {
      dispose();
    }
    finally {
      super.close();
    }
  }
}
