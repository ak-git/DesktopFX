package com.ak.comm.serial;

import org.testng.annotations.Test;

public final class ComServiceUtilsTest {
  @Test
  public void testGetPorts() {
    ComServiceUtils.PORTS.getPorts();
  }
}