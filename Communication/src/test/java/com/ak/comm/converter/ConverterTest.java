package com.ak.comm.converter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.LogRecord;
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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import static java.util.logging.Level.WARNING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ConverterTest {
  private static Path PATH;

  static {
    try {
      PATH = OutputBuilders.NONE.build(Strings.EMPTY).getPath();
    }
    catch (IOException e) {
      fail(e.getMessage(), e);
    }
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

  @Test
  void testInvalidApply() {
    Runnable runnable = () -> assertThat(INVALID_CONVERTER.apply(1)).hasSize(1);
    Consumer<LogRecord> consumer = logRecord -> assertThat(logRecord.getMessage()).isEqualTo("Invalid variables: [V1, V2] not match [1]");
    assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(
        () -> LogTestUtils.isSubstituteLogLevel(LOGGER_INVALID, WARNING, runnable, consumer)
    );
  }

  @Test
  void testValidApply() {
    assertFalse(LogTestUtils.isSubstituteLogLevel(LOGGER_VALID, WARNING,
        () -> assertThat(VALID_CONVERTER_0.apply(1)).isEmpty(),
        logRecord -> fail(logRecord.getMessage())));
  }

  @Test
  void testFrequencies() {
    assertThat(INVALID_CONVERTER.getFrequency()).isEqualTo(200);
    assertThat(VALID_CONVERTER_0.getFrequency()).isEqualTo(1000);
  }

  @AfterAll
  static void cleanUp() {
    Clean.clean(PATH);
  }

  @Test
  void testFileConvert() throws IOException {
    Path tempFile = Files.createTempFile(PATH, Strings.EMPTY, Extension.BIN.attachTo(getClass().getSimpleName()));
    Files.write(tempFile, new byte[] {51, 102, 102, 53, '\r', '\n'});
    BytesInterceptor<BufferFrame, String> interceptor = new StringBytesInterceptor(getClass().getSimpleName());
    Converter<String, ADCVariable> converter = new StringToIntegerConverter<>(ADCVariable.class, 1);
    Converter.doConvert(interceptor, converter, tempFile);
    Path out = Paths.get(Extension.CSV.attachTo(Extension.BIN.clean(tempFile.toAbsolutePath().toString())));
    List<String> result = Files.readAllLines(out, StandardCharsets.UTF_8);
    assertThat(result).hasSize(2).containsExactly("TIME,ADC", "0.0,16373.0");
  }
}