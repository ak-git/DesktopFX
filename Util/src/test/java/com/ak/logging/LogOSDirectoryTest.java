package com.ak.logging;

import com.ak.util.OS;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class LogOSDirectoryTest {
  @ParameterizedTest
  @EnumSource
  void testNames(OS os) {
    assertNotNull(LogOSDirectory.valueOf(os.name()), os::name);
  }

  @Test
  void testGetDirectory() {
    for (LogOSDirectory directory : LogOSDirectory.values()) {
      assertNotNull(directory.getDirectory());
    }
  }
}