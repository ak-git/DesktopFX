package com.ak.logging;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.StreamSupport;

import javax.annotation.Nonnull;

import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.Test;

import static com.ak.logging.LocalFileHandlerTest.delete;

public class OutputBuilderTest {
  @Nonnull
  private final Path outPath;

  private OutputBuilderTest() throws IOException {
    Path txt = new OutputBuilder("txt").fileNameWithTime(OutputBuilderTest.class.getSimpleName()).build().getPath();
    Files.createFile(txt);
    outPath = txt.getParent();
  }

  @AfterSuite
  public void setUp() throws Exception {
    delete(outPath);
  }

  @Test
  public void testLocalFileHandler() throws IOException {
    try (DirectoryStream<Path> paths = Files.newDirectoryStream(outPath, "*.txt")) {
      Assert.assertTrue(StreamSupport.stream(paths.spliterator(), true).count() > 0, outPath.toString());
    }
  }
}