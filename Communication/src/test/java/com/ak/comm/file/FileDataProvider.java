package com.ak.comm.file;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import com.ak.logging.BinaryLogBuilder;
import org.testng.annotations.DataProvider;

public final class FileDataProvider {
  private static final int KILO_BYTE = 1024;

  private FileDataProvider() {
  }

  @DataProvider(name = "files")
  public static Object[][] simple() throws IOException {
    return new Object[][] {
        {createFile(-1), -1},
        {createFile(0), 0},
        {createFile(14), 14336}
    };
  }

  @DataProvider(name = "filesCanDelete")
  public static Object[][] simple2() throws IOException {
    return new Object[][] {
        {createFile(-1), -1},
        {createFile(0), 0},
        {createFile(10), 10240}
    };
  }

  private static Path createFile(int kBytes) throws IOException {
    Path path = new BinaryLogBuilder(String.format("%s %d bytes", FileDataProvider.class.getSimpleName(), kBytes)).build().getPath();
    if (kBytes >= 0) {
      try (WritableByteChannel channel = Files.newByteChannel(path,
          StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
        ByteBuffer buffer = ByteBuffer.allocate(KILO_BYTE);
        for (int i = 0; i < KILO_BYTE; i++) {
          buffer.put((byte) i);
        }
        for (int i = 0; i < kBytes; i++) {
          buffer.rewind();
          channel.write(buffer);
        }
      }
    }
    return path;
  }
}
