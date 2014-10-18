package com.ak.storage;

import org.testng.Assert;
import org.testng.annotations.Test;

public final class LocalStorageTest {
  @Test
  public void testStorage() {
    Storage<Boolean> storage = new LocalStorage<>(LocalStorageTest.class.getSimpleName(),
        "testBooleanStorage", Boolean.class);
    for (boolean b : new boolean[] {true, false}) {
      storage.save(b);
      Assert.assertEquals(storage.get().booleanValue(), b);
    }
  }
}
