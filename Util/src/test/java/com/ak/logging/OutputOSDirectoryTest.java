package com.ak.logging;

import com.ak.util.OS;
import org.testng.Assert;
import org.testng.annotations.Test;

public class OutputOSDirectoryTest {
  private OutputOSDirectoryTest() {
  }

  @Test
  public static void testNames() {
    for (OS os : OS.values()) {
      OutputOSDirectory.valueOf(os.name());
    }
  }

  @Test
  public static void testGetDirectory() {
    for (OutputOSDirectory directory : OutputOSDirectory.values()) {
      Assert.assertNotNull(directory.getDirectory());
    }
  }
}