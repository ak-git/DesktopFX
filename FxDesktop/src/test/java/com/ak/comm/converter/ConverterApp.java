package com.ak.comm.converter;

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

import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.fx.desktop.FxClassPathXmlApplicationContext;
import com.ak.util.LineFileCollector;
import com.ak.util.Strings;
import com.sun.javafx.application.ParametersImpl;
import javafx.application.Application;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ConfigurableApplicationContext;

import static com.ak.fx.desktop.FxApplication.APP_PARAMETER_CONTEXT;

public final class ConverterApp<RESPONSE, REQUEST, EV extends Enum<EV> & Variable<EV>> implements AutoCloseable, Consumer<Path> {
  @Nonnull
  private final ConfigurableApplicationContext context;

  private ConverterApp(@Nonnull Application.Parameters parameters) {
    context = new FxClassPathXmlApplicationContext(parameters.getNamed().get(APP_PARAMETER_CONTEXT));
  }

  @Override
  public void close() {
    context.close();
  }

  @Override
  public void accept(@Nonnull Path path) {
    @SuppressWarnings("unchecked")
    BytesInterceptor<RESPONSE, REQUEST> bytesInterceptor = BeanFactoryUtils.beanOfType(context, BytesInterceptor.class);
    @SuppressWarnings("unchecked")
    Converter<RESPONSE, EV> responseConverter = BeanFactoryUtils.beanOfType(context, Converter.class);
    try (ReadableByteChannel readableByteChannel = Files.newByteChannel(path, StandardOpenOption.READ);
         LineFileCollector collector = new LineFileCollector(
             Paths.get(path.toFile().toString().replaceAll("\\.bin", Strings.EMPTY) + ".txt"),
             LineFileCollector.Direction.VERTICAL)
    ) {
      collector.accept(responseConverter.variables().stream().map(ev -> ev.toName()).collect(Collectors.joining(Strings.TAB)));
      collector.accept(Strings.EMPTY);

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

  public static void main(String[] args) {
    Application.Parameters parameters = new ParametersImpl(args);
    try (ConverterApp<?, ?, ?> app = new ConverterApp(parameters);
         DirectoryStream<Path> paths = Files.newDirectoryStream(Paths.get(Strings.EMPTY), "*.bin")) {
      paths.forEach(app);
    }
    catch (IOException e) {
      Logger.getLogger(ConverterApp.class.getName()).log(Level.WARNING, e.getMessage(), e);
    }
  }
}
