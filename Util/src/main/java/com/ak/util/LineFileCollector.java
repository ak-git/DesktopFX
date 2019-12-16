package com.ak.util;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
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

public final class LineFileCollector implements Collector<Object, BufferedWriter, Boolean>, Closeable, Consumer<String> {
  public enum Direction {
    HORIZONTAL {
      @Override
      public void acceptWriter(BufferedWriter writer) throws IOException {
        writer.write(Strings.TAB);
      }
    },
    VERTICAL {
      @Override
      public void acceptWriter(BufferedWriter writer) throws IOException {
        writer.newLine();
      }
    };

    public abstract void acceptWriter(BufferedWriter writer) throws IOException;
  }

  private final BufferedWriter writer;
  private final Direction direction;
  private boolean startFlag = true;
  private boolean errorFlag;

  public LineFileCollector(@Nonnull Path out, @Nonnull Direction direction) {
    BufferedWriter writer;
    try {
      writer = Files.newBufferedWriter(out,
          StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
    catch (IOException ex) {
      writer = new BufferedWriter(Writer.nullWriter());
    }
    this.writer = writer;
    this.direction = direction;
  }

  @Override
  public void accept(String s) {
    accumulator().accept(writer, s);
  }

  @Override
  public Supplier<BufferedWriter> supplier() {
    return () -> writer;
  }

  @Override
  public BiConsumer<BufferedWriter, Object> accumulator() {
    return (bufferedWriter, object) -> {
      if (!errorFlag) {
        try {
          if (!startFlag) {
            direction.acceptWriter(bufferedWriter);
          }
          startFlag = false;
          bufferedWriter.write(object.toString());
        }
        catch (IOException e) {
          Logger.getLogger(getClass().getName()).log(Level.WARNING, String.format("Exception when writing object: %s", object), e);
          errorFlag = true;
        }
      }
    };
  }

  @Override
  public BinaryOperator<BufferedWriter> combiner() {
    return (bufferedWriter, bufferedWriter2) -> {
      throw new UnsupportedOperationException();
    };
  }

  @Override
  public Function<BufferedWriter, Boolean> finisher() {
    return bufferedWriter -> {
      if (!errorFlag) {
        try {
          bufferedWriter.flush();
          bufferedWriter.close();
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
      writer.close();
    }
  }
}
