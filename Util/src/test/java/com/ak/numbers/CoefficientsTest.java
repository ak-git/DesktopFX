package com.ak.numbers;

import com.ak.logging.CalibrateBuilders;
import com.ak.util.Extension;
import com.ak.util.LocalIO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.annotation.Nonnegative;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CoefficientsTest {
  static Stream<Arguments> countCoefficients() {
    return Stream.of(
        arguments(InterpolatorCoefficients.INTERPOLATOR_TEST_AKIMA, 10),
        arguments(InterpolatorCoefficients.INTERPOLATOR_TEST_LINEAR, 8),
        arguments(InterpolatorCoefficients.INTERPOLATOR_TEST_INVALID, 2),
        arguments(InterpolatorCoefficients.INTERPOLATOR_FILTER_TEST_LINEAR, 4)
    );
  }

  @ParameterizedTest
  @MethodSource("countCoefficients")
  void testCoefficients(Supplier<double[]> coefficients, @Nonnegative int count) {
    assertThat(coefficients.get()).hasSize(count);
  }

  @Test
  void testRead() throws IOException {
    try (InputStream resourceAsStream = getClass().getResourceAsStream(Extension.TXT.attachTo("DIFF"))) {
      Scanner scanner = new Scanner(Objects.requireNonNull(resourceAsStream), Charset.defaultCharset());
      assertThat(Coefficients.read(scanner)).containsExactly(-1.0, 0.0, 1.0);
    }
  }

  @Nested
  class Mocking {
    private static final Logger LOGGER = Logger.getLogger(InterpolatorCoefficients.class.getName());
    private final AtomicInteger exceptionCounter = new AtomicInteger();
    @Mock
    private LocalIO localIO;

    @BeforeEach
    void setUp() {
      LOGGER.setFilter(r -> {
        assertThat(r.getThrown()).isNotNull().isInstanceOf(IOException.class);
        exceptionCounter.incrementAndGet();
        return false;
      });
      LOGGER.setLevel(Level.WARNING);
    }

    @AfterEach
    void tearDown() {
      LOGGER.setFilter(null);
      LOGGER.setLevel(Level.INFO);
    }

    @Test
    void testCoefficients() throws IOException {
      try (MockedStatic<CalibrateBuilders> mockBuilders = mockStatic(CalibrateBuilders.class)) {
        when(localIO.getPath()).thenThrow(IOException.class);
        mockBuilders.when(() -> CalibrateBuilders.build(anyString())).thenReturn(localIO);
        assertThat(InterpolatorCoefficients.INTERPOLATOR_TEST_INVALID.get()).containsExactly(1.0, 0.0);
        assertThat(exceptionCounter.get()).isOne();
      }
    }
  }
}