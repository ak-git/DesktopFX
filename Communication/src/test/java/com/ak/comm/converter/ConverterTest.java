package com.ak.comm.converter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.comm.interceptor.simple.StringBytesInterceptor;
import com.ak.comm.logging.LogTestUtils;
import com.ak.logging.OutputBuilders;
import com.ak.util.Clean;
import com.ak.util.Extension;
import com.ak.util.Strings;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.Test;

import static java.util.logging.Level.WARNING;

public class ConverterTest {
  @Nonnull
  private final Path path;

  public ConverterTest() throws IOException {
    path = OutputBuilders.NONE.build(Strings.EMPTY).getPath();
  }

  private static final Converter<Integer, TwoVariables> INVALID_CONVERTER =
      new AbstractConverter<>(TwoVariables.class, 200) {
        @Override
        protected Stream<int[]> innerApply(@Nonnull Integer integer) {
          return Stream.of(new int[] {integer});
        }
      };
  private static final Logger LOGGER_INVALID = Logger.getLogger(INVALID_CONVERTER.getClass().getName());
  private static final Converter<Integer, ADCVariable> VALID_CONVERTER_0 =
      new AbstractConverter<>(ADCVariable.class, 1000) {
        @Override
        protected Stream<int[]> innerApply(@Nonnull Integer integer) {
          return Stream.empty();
        }
      };
  private static final Logger LOGGER_VALID = Logger.getLogger(VALID_CONVERTER_0.getClass().getName());

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testInvalidApply() {
    Assert.assertTrue(LogTestUtils.isSubstituteLogLevel(LOGGER_INVALID, WARNING,
        () -> Assert.assertEquals(INVALID_CONVERTER.apply(1).count(), 1),
        logRecord -> Assert.assertEquals(logRecord.getMessage(), "Invalid variables: [V1, V2] not match [1]")));
  }

  @Test
  public void testValidApply() {
    Assert.assertFalse(LogTestUtils.isSubstituteLogLevel(LOGGER_VALID, WARNING,
        () -> Assert.assertEquals(VALID_CONVERTER_0.apply(1).count(), 0),
        logRecord -> Assert.fail(logRecord.getMessage())));
  }

  @Test
  public void testFrequencies() {
    Assert.assertEquals(INVALID_CONVERTER.getFrequency(), 200, 0.1);
    Assert.assertEquals(VALID_CONVERTER_0.getFrequency(), 1000, 0.1);
  }

  @AfterSuite
  public void cleanUp() {
    Clean.clean(path);
  }

  @Test
  public void testFileConvert() throws IOException {
    Path tempFile = Files.createTempFile(path, Strings.EMPTY, Extension.RR.attachTo(getClass().getSimpleName()));
    Files.write(tempFile, new byte[] {51, 102, 102, 53, '\r', '\n'});
    BytesInterceptor<BufferFrame, String> interceptor = new StringBytesInterceptor(getClass().getSimpleName());
    Converter<String, ADCVariable> converter = new StringToIntegerConverter<>(ADCVariable.class, 1);
    Converter.doConvert(interceptor, converter, tempFile);
    Path out = Paths.get(Extension.CSV.attachTo(Extension.RR.clean(tempFile.toAbsolutePath().toString())));
    List<String> result = Files.readAllLines(out, StandardCharsets.UTF_8);
    Assert.assertEquals(result.size(), 2);
    Assert.assertEquals(result.get(0), "ADC");
    Assert.assertEquals(result.get(1), "16373.0");
  }
}