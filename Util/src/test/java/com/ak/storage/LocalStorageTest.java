package com.ak.storage;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import org.testng.Assert;
import org.testng.annotations.Test;

public class LocalStorageTest {
  private LocalStorageTest() {
  }

  @Test
  public static void testBooleanStorage() {
    Storage<Boolean> storage = new LocalStorage<>(LocalStorageTest.class.getName(), "testBooleanStorage", Boolean.class);
    for (boolean b : new boolean[] {true, false}) {
      storage.save(b);
      Assert.assertEquals(storage.get().booleanValue(), b);
    }
    storage.delete();
  }

  @Test
  public static void testIntegerStorage() {
    Storage<Integer> storage = new LocalStorage<>(LocalStorageTest.class.getName(), "testIntStorage", Integer.class);
    for (int n : new int[] {1, -12}) {
      storage.save(n);
      Assert.assertEquals(storage.get().intValue(), n);
    }
    storage.delete();
  }

  @Test
  public static void testStringStorage() {
    Storage<String> storage = new LocalStorage<>(LocalStorageTest.class.getName(), "testStringStorage", String.class);
    for (String s : new String[] {"name", "last"}) {
      storage.save(s);
      Assert.assertEquals(storage.get(), s);
    }
    storage.delete();
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public static void testEmptyFileName() {
    new LocalStorage<>("", "testStringStorage", String.class);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public static void testNotUpdate() {
    new LocalStorage<>(LocalStorageTest.class.getName(), "testStringStorage", String.class).update("");
  }

  @Test(expectedExceptions = CloneNotSupportedException.class)
  public static void testNotClone() throws CloneNotSupportedException {
    new LocalStorage<>(LocalStorageTest.class.getName(), "testStringStorage", String.class).clone();
  }

  @Test
  public void testInvalidFileName() {
    Logger logger = Logger.getLogger(LocalStorage.class.getName());
    AtomicBoolean exceptionFlag = new AtomicBoolean(false);
    logger.setFilter(record -> {
      Assert.assertNotNull(record.getThrown());
      exceptionFlag.set(true);
      return false;
    });
    new LocalStorage<>(LocalStorageTest.class.getName(), "/invalid file ...\\\\/", String.class).save("");
    Assert.assertTrue(exceptionFlag.get(), "Exception must be thrown");
    logger.setFilter(null);
  }
}
