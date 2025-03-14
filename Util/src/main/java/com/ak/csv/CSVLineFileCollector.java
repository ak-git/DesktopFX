package com.ak.csv;

import com.ak.util.Strings;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.jspecify.annotations.Nullable;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.function.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collector;

import static java.nio.file.StandardOpenOption.*;

public final class CSVLineFileCollector implements Collector<Object[], CSVPrinter, Boolean>, Closeable, Consumer<Object[]> {
  private static final Logger LOGGER = Logger.getLogger(CSVLineFileCollector.class.getName());
  private final Path out;
  private final @Nullable Path tempFile;
  private @Nullable CSVPrinter csvPrinter;
  private boolean finished;

  public CSVLineFileCollector(Path out, String... header) {
    this.out = Objects.requireNonNull(out);
    Path temp = null;
    try {
      temp = Files.createTempFile(out.toAbsolutePath().getParent(), "%s ".formatted(out.toFile().getName()), Strings.EMPTY);
      csvPrinter = new CSVPrinter(
          Files.newBufferedWriter(temp, WRITE, CREATE, TRUNCATE_EXISTING),
          CSVFormat.Builder.create().setHeader(header.length > 0 ? header : null).get()
      );
    }
    catch (IOException ex) {
      LOGGER.log(Level.WARNING, out.toString(), ex);
      finished = true;
    }
    tempFile = temp;
  }

  @Override
  public void accept(Object[] s) {
    if (!finished) {
      try {
        Objects.requireNonNull(csvPrinter).printRecord(Objects.requireNonNull(s));
      }
      catch (IOException e) {
        LOGGER.log(Level.WARNING, "Exception when writing object: %s".formatted(Arrays.toString(s)), e);
        finished = true;
      }
    }
  }

  @Override
  public Supplier<CSVPrinter> supplier() {
    return () -> Objects.requireNonNull(csvPrinter);
  }

  @Override
  public BiConsumer<CSVPrinter, Object[]> accumulator() {
    return (_, objects) -> accept(objects);
  }

  @Override
  public BinaryOperator<CSVPrinter> combiner() {
    return (_, _) -> {
      throw new UnsupportedOperationException();
    };
  }

  @Override
  public Function<CSVPrinter, Boolean> finisher() {
    return printer -> {
      if (!printer.equals(csvPrinter)) {
        throw new IllegalArgumentException(printer.toString());
      }

      if (!finished) {
        try {
          close();
        }
        catch (IOException e) {
          LOGGER.log(Level.WARNING, e.getMessage(), e);
        }
        finally {
          finished = true;
        }
      }
      return true;
    };
  }

  @Override
  public Set<Characteristics> characteristics() {
    return Collections.emptySet();
  }

  @Override
  public void close() throws IOException {
    if (!finished) {
      Objects.requireNonNull(csvPrinter).close(true);
      Files.move(Objects.requireNonNull(tempFile), out, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }
    if (tempFile != null) {
      Files.deleteIfExists(tempFile);
    }
  }
}
