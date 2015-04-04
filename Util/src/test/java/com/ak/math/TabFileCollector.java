package com.ak.math;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
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

public final class TabFileCollector implements Collector<CharSequence, BufferedWriter, Void> {
  public enum Direction {
    HORIZONTAL(bufferedWriter -> {
      try {
        bufferedWriter.write("\t");
      }
      catch (IOException e) {
        Logger.getLogger(Direction.class.getName()).log(Level.WARNING, e.getMessage(), e);
      }
    }), VERTICAL(bufferedWriter -> {
      try {
        bufferedWriter.newLine();
      }
      catch (IOException e) {
        Logger.getLogger(Direction.class.getName()).log(Level.WARNING, e.getMessage(), e);
      }
    });

    private final Consumer<BufferedWriter> consumer;

    Direction(Consumer<BufferedWriter> consumer) {
      this.consumer = consumer;
    }
  }

  private final BufferedWriter writer;
  private final Direction direction;
  private boolean startFlag = true;

  public TabFileCollector(Path out, Direction direction) {
    BufferedWriter writer = null;
    try {
      writer = Files.newBufferedWriter(out, Charset.forName("windows-1251"),
          StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
    catch (IOException e) {
      Logger.getLogger(getClass().getName()).log(Level.WARNING, e.getMessage(), e);
    }
    this.writer = writer;
    this.direction = direction;
  }

  @Override
  public Supplier<BufferedWriter> supplier() {
    return () -> writer;
  }

  @Override
  public BiConsumer<BufferedWriter, CharSequence> accumulator() {
    return (bufferedWriter, charSequence) -> {
      try {
        if (!startFlag) {
          direction.consumer.accept(bufferedWriter);
        }
        startFlag = false;
        bufferedWriter.write(charSequence.toString());
      }
      catch (IOException e) {
        Logger.getLogger(getClass().getName()).log(Level.WARNING, e.getMessage(), e);
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
  public Function<BufferedWriter, Void> finisher() {
    return bufferedWriter -> {
      try {
        bufferedWriter.flush();
        bufferedWriter.close();
      }
      catch (IOException e) {
        Logger.getLogger(getClass().getName()).log(Level.WARNING, e.getMessage(), e);
      }
      return null;
    };
  }

  @Override
  public Set<Characteristics> characteristics() {
    return Collections.emptySet();
  }

  @Override
  protected void finalize() throws Throwable {
    try {
      writer.close();
    }
    catch (IOException e) {
      Logger.getLogger(getClass().getName()).log(Level.WARNING, e.getMessage(), e);
    }
    finally {
      super.finalize();
    }
  }
}
