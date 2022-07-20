package com.ak.comm.file;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.stream.Stream;

import com.ak.comm.converter.TwoVariables;
import com.ak.logging.LogBuilders;
import org.junit.jupiter.params.provider.Arguments;

import static org.junit.jupiter.params.provider.Arguments.arguments;

public class FileDataProvider {
  private FileDataProvider() {
  }

  public static Stream<Path> parallelRampFiles() throws IOException {
    return Stream.of(createFile(11), createFile(11), createFile(2), createFile(3));
  }

  public static Stream<Arguments> rampFile() throws IOException {
    return Stream.of(
        arguments(createFile(16), 4096, true),
        arguments(createFile(16), 16371, false),
        arguments(createFile(16), 0, false),
        arguments(createFile(4), 4086, false)
    );
  }

  public static Stream<Arguments> rampFiles() throws IOException {
    return Stream.of(
        arguments(createFile(-1), -1),
        arguments(createFile(0), 0),
        arguments(createFile(14), 14328),
        arguments(createFile(14), 0)
    );
  }

  public static Stream<Path> rampFiles2() throws IOException {
    return Stream.of(createFile(111), createFile(21), createFile(31));
  }

  public static Stream<Arguments> filesCanDelete() throws IOException {
    return Stream.of(
        arguments(createFile(-1), -1),
        arguments(createFile(0), 0),
        arguments(createFile(10), 10233)
    );
  }

  private static Path createFile(int kBytes) throws IOException {
    Path path = LogBuilders.SIMPLE.build("%s %d kBytes".formatted(FileDataProvider.class.getSimpleName(), kBytes)).getPath();
    if (kBytes >= 0) {
      try (WritableByteChannel channel = Files.newByteChannel(path,
          StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
        ByteBuffer buffer = ByteBuffer.allocate(1 + TwoVariables.values().length * Integer.BYTES).order(ByteOrder.LITTLE_ENDIAN);

        int ramp = 0;
        for (int i = 0; i < kBytes * 1024 / buffer.limit(); i++) {
          buffer.clear();
          buffer.put((byte) (ramp++));
          for (TwoVariables v : TwoVariables.values()) {
            buffer.putInt(i + v.ordinal());
          }
          buffer.rewind();
          channel.write(buffer);
        }
      }
    }
    return path;
  }
}
