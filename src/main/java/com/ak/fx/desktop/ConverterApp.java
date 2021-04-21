package com.ak.fx.desktop;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.inject.Provider;

import com.ak.comm.converter.Converter;
import com.ak.comm.converter.TimeVariable;
import com.ak.comm.converter.Variable;
import com.ak.comm.converter.Variables;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.util.CSVLineFileCollector;
import com.ak.util.Extension;
import com.ak.util.Strings;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class ConverterApp implements AutoCloseable, Consumer<Path> {
  private static final Logger LOGGER = Logger.getLogger(ConverterApp.class.getName());
  @Nonnull
  private final ConfigurableApplicationContext context;

  public ConverterApp(@Nonnull ConfigurableApplicationContext context) {
    this.context = context;
  }

  @Override
  public void close() {
    context.close();
  }

  public static void main(String[] args) {
    try (var app = new ConverterApp(SpringApplication.run(ConverterApp.class, args));
         DirectoryStream<Path> paths = Files.newDirectoryStream(Paths.get(Strings.EMPTY), Extension.BIN.attachTo("*"))) {
      paths.forEach(app);
    }
    catch (IOException e) {
      LOGGER.log(Level.WARNING, e.getMessage(), e);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public void accept(@Nonnull Path path) {
    doConvert(() -> context.getBean(BytesInterceptor.class), () -> context.getBean(Converter.class), path);
  }

  @ParametersAreNonnullByDefault
  public static <T, R, V extends Enum<V> & Variable<V>> void doConvert(Provider<BytesInterceptor<T, R>> interceptorProvider,
                                                                       Provider<Converter<R, V>> converterProvider, Path path) {
    BytesInterceptor<T, R> bytesInterceptor = interceptorProvider.get();
    Converter<R, V> responseConverter = converterProvider.get();
    String fileName = path.toFile().getPath();
    if (fileName.endsWith(Extension.BIN.attachTo(bytesInterceptor.name()))) {
      try (ReadableByteChannel readableByteChannel = Files.newByteChannel(path, StandardOpenOption.READ);
           var collector = new CSVLineFileCollector(Paths.get(Extension.CSV.attachTo(Extension.BIN.clean(fileName))),
               Stream.concat(
                   Stream.of(TimeVariable.TIME).map(Variables::toName),
                   responseConverter.variables().stream().map(Variables::toName)
               ).toArray(String[]::new))
      ) {
        var buffer = ByteBuffer.allocate((int) Files.getFileStore(path).getBlockSize());
        var timeCounter = new AtomicInteger();
        while (readableByteChannel.read(buffer) > 0) {
          buffer.flip();
          bytesInterceptor.apply(buffer).flatMap(responseConverter).forEach(
              ints -> {
                var time = BigDecimal.valueOf(timeCounter.getAndIncrement() / responseConverter.getFrequency())
                    .setScale(3, RoundingMode.HALF_EVEN).doubleValue();
                collector.accept(Stream.concat(Stream.of(time), Arrays.stream(ints).boxed()).toArray());
              }
          );
          buffer.clear();
        }
        LOGGER.info(() -> "Converted %s as '%s'".formatted(path, bytesInterceptor.name()));
      }
      catch (IOException e) {
        LOGGER.log(Level.WARNING, e.getMessage(), e);
      }
    }
  }
}
