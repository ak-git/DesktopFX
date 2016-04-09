package com.ak.comm.file;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import com.ak.logging.BinaryLogBuilder;
import com.ak.logging.LocalFileHandler;
import com.ak.logging.LogPathBuilder;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import rx.observers.TestSubscriber;

public final class FileServiceTest {
  @Test
  public void testInvalidFile() throws IOException {
    Path path = new BinaryLogBuilder(getClass().getSimpleName(), LocalFileHandler.class).build().getPath();
    FileService service = new FileService(path);
    TestSubscriber<ByteBuffer> testSubscriber = TestSubscriber.create();
    service.getBufferObservable().subscribe(testSubscriber);
    service.open();
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
    try (FileService service = new FileService(path)) {
      service.getBufferObservable().subscribe(testSubscriber);
      service.open();
      testSubscriber.assertNoErrors();
      testSubscriber.assertNotCompleted();
    }
    testSubscriber.assertCompleted();
    Files.deleteIfExists(path);
  }

  @AfterClass
  @BeforeClass
  public void tearDown() throws Exception {
    Assert.assertTrue(Files.deleteIfExists(new LogPathBuilder().addPath(LocalFileHandler.class.getSimpleName()).build().getPath()));
  }
}