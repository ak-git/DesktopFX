package com.ak.storage;

import java.util.Random;

import org.testng.Assert;
import org.testng.annotations.Test;

public final class GenericStorageTest {
  @Test
  public void testBooleanStorage() {
    Storage<Boolean> storage = GenericStorage.newBooleanStorage(GenericStorageTest.class.getSimpleName(),
        "testBooleanStorage", Math.random() > 0.5);
    for (boolean b : new boolean[] {true, false}) {
      storage.save(b);
      Assert.assertEquals(storage.load(true).booleanValue(), b);
    }
  }

  @Test
  public void testIntegerStorage() {
    Storage<Integer> storage = GenericStorage.newIntegerStorage(GenericStorageTest.class.getSimpleName(),
        "testIntegerStorage", new Random().nextInt());
    for (int n : new int[] {1, 2, 3}) {
      storage.save(n);
      Assert.assertEquals(storage.load(n).intValue(), n);
    }
  }

  @Test
  public void testStringStorage() {
    Storage<String> storage = GenericStorage.newStringStorage(GenericStorageTest.class.getSimpleName(),
        "testStringStorage");
    for (String s : new String[] {"abc", "df"}) {
      storage.save(s);
      Assert.assertEquals(storage.load(s), s);
    }
  }
}
