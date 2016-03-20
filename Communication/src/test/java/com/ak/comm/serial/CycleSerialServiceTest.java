package com.ak.comm.serial;

import com.ak.comm.interceptor.DefaultBytesInterceptor;
import org.testng.annotations.Test;

public final class CycleSerialServiceTest {
  @Test
  public void test() {
    CycleSerialService<Integer> service = new CycleSerialService<>(115200, new DefaultBytesInterceptor());
    service.close();
  }
}