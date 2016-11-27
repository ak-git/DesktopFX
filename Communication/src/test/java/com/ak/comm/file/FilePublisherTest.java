package com.ak.comm.file;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.logging.BinaryLogBuilder;
import com.ak.logging.LocalFileHandler;
import io.reactivex.Flowable;
import io.reactivex.subscribers.TestSubscriber;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public final class FilePublisherTest {
  private static final int KILO_BYTE = 1024;

  @DataProvider(name = "files")
  public Object[][] simple() throws IOException {
    return new Object[][] {
        {createFile(-1), 0},
        {createFile(0), 0},
        {createFile(14), 14 / 4 + 1}
    };
  }

  @Test(dataProvider = "files")
  public void testFile(@Nonnull Path fileToRead, @Nonnegative int valueCount) throws Exception {
    TestSubscriber<ByteBuffer> testSubscriber = TestSubscriber.create();
    Flowable.fromPublisher(new FilePublisher(fileToRead)).subscribe(testSubscriber);
    testSubscriber.assertValueCount(valueCount);
    testSubscriber.assertComplete();
    testSubscriber.assertNoErrors();
    Files.deleteIfExists(fileToRead);
  }

  private Path createFile(int kBytes) throws IOException {
    Path path = new BinaryLogBuilder(String.format("%s %d bytes", getClass().getSimpleName(), kBytes), LocalFileHandler.class).build().getPath();
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