package com.ak.comm.file;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import com.ak.comm.converter.TwoVariables;
import com.ak.logging.BinaryLogBuilder;
import org.testng.annotations.DataProvider;

public final class FileDataProvider {
  private FileDataProvider() {
  }

  @DataProvider(name = "files")
  public static Object[][] simple() throws IOException {
    return new Object[][] {
        {createFile(-1), -1},
        {createFile(0), 0},
        {createFile(14), 14328}
    };
  }

  @DataProvider(name = "filesCanDelete")
  public static Object[][] simple2() throws IOException {
    return new Object[][] {
        {createFile(-1), -1},
        {createFile(0), 0},
        {createFile(10), 10233}
    };
  }

  private static Path createFile(int kBytes) throws IOException {
    Path path = new BinaryLogBuilder(String.format("%s %d bytes", FileDataProvider.class.getSimpleName(), kBytes)).build().getPath();
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
