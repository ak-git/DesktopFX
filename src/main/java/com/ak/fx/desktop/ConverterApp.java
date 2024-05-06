package com.ak.fx.desktop;

import com.ak.comm.converter.Converter;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.util.Extension;
import com.ak.util.Strings;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

@SpringBootApplication
public class ConverterApp implements AutoCloseable, Consumer<Path> {
  private static final Logger LOGGER = Logger.getLogger(ConverterApp.class.getName());
  private final ConfigurableApplicationContext context;

  public ConverterApp(ConfigurableApplicationContext context) {
    this.context = Objects.requireNonNull(context);
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
    finally {
      LOGGER.info(() -> "Conversion finished");
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public void accept(Path path) {
    Converter.doConvert(context.getBean(BytesInterceptor.class), context.getBean(Converter.class), path);
  }
}
