package com.ak.comm.converter;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.measure.IncommensurableException;

import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.util.CSVLineFileCollector;
import com.ak.util.Extension;

public interface Converter<R, V extends Enum<V> & Variable<V>> extends Function<R, Stream<int[]>>, Refreshable {
  @Nonnull
  List<V> variables();

  @Nonnegative
  double getFrequency();

  @ParametersAreNonnullByDefault
  static <T, R, V extends Enum<V> & Variable<V>> void doConvert(BytesInterceptor<T, R> bytesInterceptor,
                                                                Converter<R, V> responseConverter, Path path) {
    String fileName = path.toFile().getPath();
    Path out = Paths.get(Extension.CSV.attachTo(Extension.BIN.clean(fileName)));
    if (Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS) &&
        fileName.endsWith(Extension.BIN.attachTo(bytesInterceptor.name())) &&
        Files.notExists(out)) {
      try (ReadableByteChannel readableByteChannel = Files.newByteChannel(path, StandardOpenOption.READ);
           var collector = new CSVLineFileCollector(out,
               Stream.concat(
                   Stream.of(TimeVariable.TIME).map(Variable::name),
                   responseConverter.variables().stream().map(Variable::name)
               ).toArray(String[]::new))
      ) {
        var buffer = ByteBuffer.allocate((int) Files.getFileStore(path).getBlockSize());
        var timeCounter = new AtomicInteger();

        double[] pow = responseConverter.variables().stream().map(Variable::getUnit)
            .mapToDouble(unit -> {
              try {
                return Math.min(unit.getSystemUnit().getConverterToAny(unit).convert(1.0), 1000.0);
              }
              catch (IncommensurableException e) {
                return 1.0;
              }
            })
            .toArray();

        while (readableByteChannel.read(buffer) > 0) {
          buffer.flip();
          bytesInterceptor.apply(buffer).flatMap(responseConverter)
              .map(ints -> {
                double[] doubles = new double[ints.length];
                for (int i = 0; i < ints.length; i++) {
                  doubles[i] = round3(ints[i] / pow[i]);
                }
                return doubles;
              })
              .forEach(
                  doubles -> {
                    var time = round3(timeCounter.getAndIncrement() / responseConverter.getFrequency());
                    collector.accept(Stream.concat(Stream.of(time), Arrays.stream(doubles).boxed()).toArray());
                  }
              );
          buffer.clear();
        }
        Logger.getLogger(Converter.class.getName()).info(() -> "Converted %s as '%s'".formatted(path, bytesInterceptor.name()));
      }
      catch (IOException e) {
        Logger.getLogger(Converter.class.getName()).log(Level.WARNING, e.getMessage(), e);
      }
    }
  }

  private static double round3(double d) {
    return BigDecimal.valueOf(d).setScale(3, RoundingMode.HALF_EVEN).doubleValue();
  }
}
