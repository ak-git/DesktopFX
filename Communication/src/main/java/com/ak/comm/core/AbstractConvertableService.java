package com.ak.comm.core;

import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;

import com.ak.comm.converter.Converter;
import com.ak.comm.converter.Variable;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.logging.BinaryLogBuilder;

public abstract class AbstractConvertableService<RESPONSE, REQUEST, EV extends Enum<EV> & Variable> extends AbstractService {
  @Nonnull
  private final BytesInterceptor<RESPONSE, REQUEST> bytesInterceptor;
  @Nonnull
  private final Converter<RESPONSE, EV> responseConverter;
  @Nonnull
  private final SafeByteChannel byteChannel = new SafeByteChannel(() -> {
    Path path = new BinaryLogBuilder().fileNameWithTime(getClass().getSimpleName()).build().getPath();
    return Files.newByteChannel(path, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE, StandardOpenOption.READ);
  });
  @Nonnull
  private final ByteBuffer workingBuffer;

  public AbstractConvertableService(@Nonnull BytesInterceptor<RESPONSE, REQUEST> bytesInterceptor,
                                    @Nonnull Converter<RESPONSE, EV> responseConverter) {
    this.bytesInterceptor = bytesInterceptor;
    this.responseConverter = responseConverter;
    workingBuffer = ByteBuffer.allocate(responseConverter.variables().size() * Integer.BYTES);
  }

  @OverridingMethodsMustInvokeSuper
  @Override
  public void close() {
    byteChannel.close();
  }

  protected final Stream<int[]> process(@Nonnull ByteBuffer buffer) {
    return bytesInterceptor.apply(buffer).flatMap(responseConverter::apply).peek(ints -> {
      workingBuffer.clear();
      for (int i : ints) {
        workingBuffer.putInt(i);
      }
      workingBuffer.flip();
      byteChannel.write(workingBuffer);
    });
  }

  protected final BytesInterceptor<RESPONSE, REQUEST> bytesInterceptor() {
    return bytesInterceptor;
  }
}