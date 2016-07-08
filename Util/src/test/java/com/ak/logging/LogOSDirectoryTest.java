package com.ak.logging;

import com.ak.util.OS;
import org.testng.Assert;
import org.testng.annotations.Test;

public class LogOSDirectoryTest {
  private LogOSDirectoryTest() {
  }

  @Test
  public static void testNames() {
    for (OS os : OS.values()) {
      LogOSDirectory.valueOf(os.name());
    }
  }

  @Test
  public static void testGetDirectory() {
    for (LogOSDirectory directory : LogOSDirectory.values()) {
      Assert.assertNotNull(directory.getDirectory());
    }
  }
}