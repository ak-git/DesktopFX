package com.ak.comm.converter;

import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.csv.CSVLineFileCollector;
import com.ak.util.Extension;
import com.ak.util.UIConstants;
import tec.uom.se.AbstractUnit;
import tec.uom.se.function.RationalConverter;

import javax.annotation.Nonnegative;
import javax.measure.IncommensurableException;
import javax.measure.UnitConverter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public interface Converter<R, V extends Enum<V> & Variable<V>> extends Function<R, Stream<int[]>>, Refreshable {
  List<V> variables();

  @Nonnegative
  double getFrequency();

  static <T, R, V extends Enum<V> & Variable<V>> void doConvert(BytesInterceptor<T, R> bytesInterceptor,
                                                                Converter<R, V> responseConverter, Path path) {
    if (Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS) && path.toString().lastIndexOf(bytesInterceptor.name()) != -1) {
      final int TRIES = 2;
      for (int i = 0; i < TRIES && !isProcessed(bytesInterceptor, responseConverter, path); i++) {
        try {
          TimeUnit.SECONDS.sleep(UIConstants.UI_DELAY.getSeconds());
        }
        catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          break;
        }
      }
    }
  }

  private static <T, R, V extends Enum<V> & Variable<V>> boolean isProcessed(BytesInterceptor<T, R> bytesInterceptor,
                                                                             Converter<R, V> responseConverter, Path path) {
    Path out = Paths.get(Extension.CSV.attachTo(Extension.BIN.clean(path.toString())));
    if (Files.notExists(out)) {
      try (ReadableByteChannel readableByteChannel = Files.newByteChannel(path, StandardOpenOption.READ);
           var collector = new CSVLineFileCollector(out,
               Stream.concat(
                   Stream.of(TimeVariable.TIME)
                       .map(v -> "%s_%s".formatted(v.name(), v.getUnit())),
                   responseConverter.variables().stream()
                       .map(v -> {
                         if (Objects.equals(v.getUnit(), AbstractUnit.ONE)) {
                           return v.name();
                         }
                         else {
                           return "%s_%s".formatted(v.name(), Variables.fixUnit(Variables.tryToUp3(v.getUnit())));
                         }
                       })
               ).toArray(String[]::new))
      ) {
        var buffer = ByteBuffer.allocate((int) Files.getFileStore(path).getBlockSize());
        var timeCounter = new AtomicInteger();

        List<UnitConverter> unitConverters = responseConverter.variables().stream().map(v -> {
          try {
            return v.getUnit().getConverterToAny(Variables.tryToUp3(v.getUnit()));
          }
          catch (IncommensurableException e) {
            return new RationalConverter(1, 1);
          }
        }).toList();

        while (readableByteChannel.read(buffer) > 0) {
          buffer.flip();
          bytesInterceptor.apply(buffer).flatMap(responseConverter)
              .forEach(
                  ints -> {
                    var iterator = unitConverters.iterator();
                    collector.accept(
                        Stream.concat(
                            Stream.of(round3(timeCounter.getAndIncrement() / responseConverter.getFrequency())),
                            Arrays.stream(ints).mapToDouble(value -> round3(iterator.next().convert(value))).boxed()
                        ).toArray()
                    );
                  }
              );
          buffer.clear();
        }
        Logger.getLogger(Converter.class.getName()).info(() -> "Converted %s as '%s'".formatted(path, bytesInterceptor.name()));
        return true;
      }
      catch (FileSystemException e) {
        Logger.getLogger(Converter.class.getName()).log(Level.CONFIG, e.getMessage(), e);
        return false;
      }
      catch (IOException e) {
        Logger.getLogger(Converter.class.getName()).log(Level.WARNING, e.getMessage(), e);
        return true;
      }
    }
    return true;
  }

  private static double round3(double d) {
    return BigDecimal.valueOf(d).setScale(3, RoundingMode.HALF_EVEN).doubleValue();
  }
}
