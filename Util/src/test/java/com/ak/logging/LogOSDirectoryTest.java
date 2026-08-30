package com.ak.logging;

import com.ak.util.OS;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class LogOSDirectoryTest {
  @ParameterizedTest
  @EnumSource
  void names(OS os) {
    assertThat(LogOSDirectory.valueOf(os.name())).as(os::name).isNotNull();
  }

  @Test
  void getDirectory() {
    for (LogOSDirectory directory : LogOSDirectory.values()) {
      assertThat(directory.getDirectory()).isNotNull();
    }
  }
}