package com.ak.logging;

import com.ak.util.OS;
import org.testng.Assert;
import org.testng.annotations.Test;

public class OutputOSDirectoryTest {
  @Test
  public void testNames() {
    for (OS os : OS.values()) {
      OutputOSDirectory.valueOf(os.name());
    }
  }

  @Test
  public void testGetDirectory() {
    for (OutputOSDirectory directory : OutputOSDirectory.values()) {
      Assert.assertNotNull(directory.getDirectory());
    }
  }
}