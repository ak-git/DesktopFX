package com.ak.logging;

import com.ak.util.OS;
import org.testng.Assert;
import org.testng.annotations.Test;

public class LogOSDirectoryTest {
  @Test
  public void testNames() {
    for (OS os : OS.values()) {
      LogOSDirectory.valueOf(os.name());
    }
  }

  @Test
  public void testGetDirectory() {
    for (LogOSDirectory directory : LogOSDirectory.values()) {
      Assert.assertNotNull(directory.getDirectory());
    }
  }
}