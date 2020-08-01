package com.ak.fx.desktop;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import com.ak.comm.converter.Converter;
import com.ak.comm.converter.Variable;
import com.ak.comm.converter.Variables;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.util.Extension;
import com.ak.util.LineFileCollector;
import com.ak.util.Strings;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication(scanBasePackages = {"com.ak.comm.interceptor", "com.ak.comm.converter"})
public class ConverterApp<T, R, V extends Enum<V> & Variable<V>> implements AutoCloseable, Consumer<Path> {
  private final ConfigurableApplicationContext context;

  public ConverterApp(ConfigurableApplicationContext context) {
    this.context = context;
  }

  @Override
  public void close() {
    context.close();
  }

  @SuppressWarnings("rawtypes")
  public static void main(String[] args) {
    try (ConverterApp<?, ?, ?> app = new ConverterApp(SpringApplication.run(ConverterApp.class, args));
         DirectoryStream<Path> paths = Files.newDirectoryStream(Paths.get(Strings.EMPTY), "*.bin")) {
      paths.forEach(app);
    }
    catch (IOException e) {
      Logger.getLogger(ConverterApp.class.getName()).log(Level.WARNING, e.getMessage(), e);
    }
  }

  @Override
  public void accept(@Nonnull Path path) {
    @SuppressWarnings("unchecked")
    BytesInterceptor<T, R> bytesInterceptor = context.getBean(BytesInterceptor.class);
    @SuppressWarnings("unchecked")
    Converter<R, V> responseConverter = context.getBean(Converter.class);
    try (ReadableByteChannel readableByteChannel = Files.newByteChannel(path, StandardOpenOption.READ);
         LineFileCollector collector = new LineFileCollector(
             Paths.get(Extension.TXT.attachTo(path.toFile().toString().replace("\\.bin", Strings.EMPTY))),
             LineFileCollector.Direction.VERTICAL)
    ) {
      collector.accept(responseConverter.variables().stream().map(Variables::toName).collect(Collectors.joining(Strings.TAB)));

      ByteBuffer buffer = ByteBuffer.allocate(1024);
      while (readableByteChannel.read(buffer) > 0) {
        buffer.flip();
        bytesInterceptor.apply(buffer).flatMap(responseConverter).forEach(ints ->
            collector.accept(Arrays.stream(ints).mapToObj(Integer::toString).collect(Collectors.joining(Strings.TAB))));
        buffer.clear();
      }
    }
    catch (IOException e) {
      Logger.getLogger(getClass().getName()).log(Level.INFO, e.getMessage(), e);
    }
  }
}
