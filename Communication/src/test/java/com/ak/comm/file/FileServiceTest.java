package com.ak.comm.file;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import com.ak.comm.core.Service;
import com.ak.logging.BinaryLogBuilder;
import com.ak.logging.LocalFileHandler;
import com.ak.logging.LogPathBuilder;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;
import rx.observers.TestSubscriber;

public final class FileServiceTest {
  @Test
  public void testInvalidFile() throws IOException {
    Path path = new BinaryLogBuilder(getClass().getSimpleName(), LocalFileHandler.class).build().getPath();
    TestSubscriber<ByteBuffer> testSubscriber = TestSubscriber.create();
    Service<ByteBuffer> service = new FileService(path, testSubscriber);
    service.getBufferObservable().subscribe(testSubscriber);
    testSubscriber.assertNoValues();
    testSubscriber.assertNoErrors();
    testSubscriber.assertNotCompleted();
    service.close();
    testSubscriber.assertUnsubscribed();
    Files.deleteIfExists(path);
  }

  @Test
  public void testFile() throws Exception {
    Path path = new BinaryLogBuilder(getClass().getSimpleName(), LocalFileHandler.class).build().getPath();
    try (WritableByteChannel channel = Files.newByteChannel(path,
        StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
      ByteBuffer buffer = ByteBuffer.wrap(new byte[] {'a', 'b', 'c', '1', '2', '3', '\n'});
      for (int i = 0; i < 1024; i++) {
        channel.write(buffer);
        buffer.rewind();
      }
    }

    TestSubscriber<ByteBuffer> testSubscriber = TestSubscriber.create();
    try (Service<ByteBuffer> service = new FileService(path, testSubscriber)) {
      service.getBufferObservable().subscribe(testSubscriber);
      testSubscriber.assertNoErrors();
      testSubscriber.assertValueCount(2);
      testSubscriber.assertCompleted();
    }
    Files.deleteIfExists(path);
  }

  @AfterClass
  public void tearDown() throws Exception {
    Path logPath = new LogPathBuilder().addPath(LocalFileHandler.class.getSimpleName()).build().getPath();
    try (DirectoryStream<Path> ds = Files.newDirectoryStream(logPath)) {
      for (Path file : ds) {
        Files.delete(file);
      }
      Files.delete(logPath);
    }
  }
}