package com.ak.logging;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Nonnull;

import com.ak.util.Extension;
import org.testng.Assert;
import org.testng.annotations.Test;

public class OutputBuildersTest {
  @Test
  public void testOutputBuilderCSV() throws IOException, NoSuchAlgorithmException {
    String fileName = randomFileName();
    Path path = OutputBuilders.CSV.build(fileName).getPath();
    Assert.assertTrue(path.toFile().getName().endsWith(Extension.CSV.name().toLowerCase()), path.toString());
  }

  @Test
  public void testOutputBuilderNone() throws IOException, NoSuchAlgorithmException {
    String fileName = randomFileName();
    Path path = OutputBuilders.NONE.build(fileName).getPath();
    Assert.assertFalse(path.toFile().getName().endsWith(Extension.CSV.name().toLowerCase()), path.toString());
  }

  public static String randomFileName() throws NoSuchAlgorithmException {
    return digestToString(MessageDigest.getInstance("SHA-512").digest(Instant.now().toString().getBytes(Charset.defaultCharset())));
  }

  private static String digestToString(@Nonnull byte[] digest) {
    return IntStream.range(0, digest.length).filter(value -> value % 4 == 0)
        .mapToObj(i -> "%02x".formatted(digest[i])).collect(Collectors.joining());
  }
}