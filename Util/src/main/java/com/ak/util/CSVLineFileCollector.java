package com.ak.util;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

public final class CSVLineFileCollector implements Collector<Object[], CSVPrinter, Boolean>, Closeable, Consumer<Object[]> {
  @Nullable
  private CSVPrinter csvPrinter;
  private boolean errorFlag;

  public CSVLineFileCollector(@Nonnull Path out, @Nonnull String... header) {
    try {
      csvPrinter = new CSVPrinter(
          Files.newBufferedWriter(out, WRITE, CREATE, TRUNCATE_EXISTING),
          CSVFormat.DEFAULT.withHeader(header.length > 0 ? header : null)
      );
    }
    catch (IOException ex) {
      Logger.getLogger(getClass().getName()).log(Level.WARNING, out.toString(), ex);
      errorFlag = true;
    }
  }

  @Override
  public void accept(@Nonnull Object[] s) {
    if (!errorFlag) {
      try {
        Objects.requireNonNull(csvPrinter).printRecord(s);
      }
      catch (IOException e) {
        Logger.getLogger(getClass().getName()).log(Level.WARNING, "Exception when writing object: %s".formatted(Arrays.toString(s)), e);
        errorFlag = true;
      }
    }
  }

  @Override
  public Supplier<CSVPrinter> supplier() {
    return () -> Objects.requireNonNull(csvPrinter);
  }

  @Override
  public BiConsumer<CSVPrinter, Object[]> accumulator() {
    return (p, objects) -> accept(objects);
  }

  @Override
  public BinaryOperator<CSVPrinter> combiner() {
    return (p1, p2) -> {
      throw new UnsupportedOperationException();
    };
  }

  @Override
  public Function<CSVPrinter, Boolean> finisher() {
    return printer -> {
      if (!errorFlag) {
        try {
          printer.flush();
          printer.close();
        }
        catch (IOException e) {
          Logger.getLogger(getClass().getName()).log(Level.WARNING, e.getMessage(), e);
          errorFlag = true;
        }
      }
      return !errorFlag;
    };
  }

  @Override
  public Set<Characteristics> characteristics() {
    return Collections.emptySet();
  }

  @Override
  public void close() throws IOException {
    if (!errorFlag) {
      Objects.requireNonNull(csvPrinter).close();
    }
  }
}
