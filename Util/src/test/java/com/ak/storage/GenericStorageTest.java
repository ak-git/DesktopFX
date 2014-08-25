package com.ak.storage;

import org.testng.Assert;
import org.testng.annotations.Test;

public final class GenericStorageTest {
  @Test
  public void testLoad() {
    Storage<Boolean> storage = GenericStorage.newBooleanStorage(GenericStorageTest.class.getSimpleName(), "testLoad", true);
    for (boolean b : new boolean[] {true, false}) {
      storage.save(b);
      Assert.assertEquals(storage.load(true).booleanValue(), b);
    }
  }
}
