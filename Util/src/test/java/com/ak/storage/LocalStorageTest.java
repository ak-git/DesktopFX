package com.ak.storage;

import org.testng.Assert;
import org.testng.annotations.Test;

public class LocalStorageTest {
  private LocalStorageTest() {
  }

  @Test
  public static void testBooleanStorage() {
    Storage<Boolean> storage = new LocalStorage<>(LocalStorageTest.class.getSimpleName(),
        "testBooleanStorage", Boolean.class);
    for (boolean b : new boolean[] {true, false}) {
      storage.save(b);
      Assert.assertEquals(storage.get().booleanValue(), b);
    }
    storage.delete();
  }

  @Test
  public static void testIntegerStorage() {
    Storage<Integer> storage = new LocalStorage<>(LocalStorageTest.class.getSimpleName(),
        "testIntStorage", Integer.class);
    for (int n : new int[] {1, 12}) {
      storage.save(n);
      Assert.assertEquals(storage.get().intValue(), n);
    }
    storage.delete();
  }

  @Test
  public static void testStringStorage() {
    Storage<String> storage = new LocalStorage<>(LocalStorageTest.class.getSimpleName(),
        "testStringStorage", String.class);
    for (String s : new String[] {"name", "last"}) {
      storage.save(s);
      Assert.assertEquals(storage.get(), s);
    }
    storage.delete();
  }
}
