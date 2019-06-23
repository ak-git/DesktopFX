package com.ak.comm.file;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import com.ak.comm.converter.TwoVariables;
import com.ak.logging.LogBuilders;
import org.testng.annotations.DataProvider;

public class FileDataProvider {
  private FileDataProvider() {
  }

  @DataProvider(name = "parallelRampFiles", parallel = true)
  public static Object[][] parallelRampFiles() throws IOException {
    return new Object[][] {
        {createFile(11)},
        {createFile(11)},
        {createFile(2)},
        {createFile(3)},
    };
  }

  @DataProvider(name = "rampFile")
  public static Object[][] rampFile() throws IOException {
    return new Object[][] {
        {createFile(16), 4096, true},
        {createFile(16), 16371, false},
        {createFile(16), 0, false},
        {createFile(4), 4086, false},
    };
  }

  @DataProvider(name = "rampFiles")
  public static Object[][] rampFiles() throws IOException {
    return new Object[][] {
        {createFile(-1), -1},
        {createFile(0), 0},
        {createFile(14), 14328},
        {createFile(14), 0}
    };
  }

  @DataProvider(name = "rampFiles2")
  public static Object[][] rampFiles2() throws IOException {
    return new Object[][] {
        {createFile(111)},
        {createFile(21)},
        {createFile(31)},
    };
  }

  @DataProvider(name = "filesCanDelete")
  public static Object[][] filesCanDelete() throws IOException {
    return new Object[][] {
        {createFile(-1), -1},
        {createFile(0), 0},
        {createFile(10), 10233}
    };
  }

  private static Path createFile(int kBytes) throws IOException {
    Path path = LogBuilders.SIMPLE.build(String.format("%s %d kBytes", FileDataProvider.class.getSimpleName(), kBytes)).getPath();
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
