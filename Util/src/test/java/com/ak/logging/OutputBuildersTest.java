package com.ak.logging;

import com.ak.util.Extension;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertFalse;

class OutputBuildersTest {
  @Test
  void testOutputBuilderDate() throws IOException, NoSuchAlgorithmException {
    String fileName = randomFileName();
    Path path = OutputBuilders.NONE_WITH_DATE.build(fileName).getPath();
    assertFalse(path.toFile().getName().contains("."), path.toString());
  }

  @Test
  void testOutputBuilderNone() throws IOException, NoSuchAlgorithmException {
    String fileName = randomFileName();
    Path path = OutputBuilders.NONE.build(fileName).getPath();
    assertFalse(path.toFile().getName().endsWith(Extension.CSV.name().toLowerCase()), path.toString());
  }

  public static String randomFileName() throws NoSuchAlgorithmException {
    return digestToString(MessageDigest.getInstance("SHA-512").digest(Instant.now().toString().getBytes(Charset.defaultCharset())));
  }

  private static String digestToString(byte[] digest) {
    return IntStream.range(0, digest.length).filter(value -> value % 4 == 0)
        .mapToObj(i -> "%02x".formatted(digest[i])).collect(Collectors.joining());
  }
}