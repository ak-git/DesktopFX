package com.ak.rsm2;

import com.ak.util.Metrics;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.*;

class PhiTest {
  @ParameterizedTest
  @CsvSource(delimiter = '|', textBlock = """
      10 | 10 | 20 | 0.666
      10 | 20 | 10 | 0.666
      """)
  void value(double h, double sPU, double lCC, double expectedPhi) {
    double hSI = Metrics.Length.MILLI.toSI(h);
    ElectrodeSystem.Tetrapolar tetrapolar = ElectrodeSystem.ofMilli().tetrapolar(sPU, lCC).build();
    assertThat(Phi.of(hSI, tetrapolar).value()).isCloseTo(expectedPhi, byLessThan(0.001));
  }

  @ParameterizedTest
  @CsvSource(delimiter = '|', value = "10 | 20")
  void invalidH(double sPU, double lCC) {
    ElectrodeSystem.Tetrapolar tetrapolar = ElectrodeSystem.ofMilli().tetrapolar(sPU, lCC).build();
    assertThatIllegalArgumentException().isThrownBy(() -> Phi.of(-0.1, tetrapolar).value());
  }
}