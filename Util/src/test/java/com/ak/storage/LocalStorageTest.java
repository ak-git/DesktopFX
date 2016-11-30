package com.ak.storage;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import org.testng.Assert;
import org.testng.annotations.Test;

import static com.ak.util.Strings.EMPTY;

public class LocalStorageTest {
  private static final Logger LOGGER = Logger.getLogger(LocalStorage.class.getName());

  private LocalStorageTest() {
  }

  @Test
  public static void testBooleanStorage() {
    Storage<Boolean> storage = new LocalStorage<>(LocalStorageTest.class.getName(), "testBooleanStorage", Boolean.class);
    for (boolean b : new boolean[] {true, false}) {
      storage.save(b);
      Assert.assertEquals(Optional.ofNullable(storage.get()).orElseThrow(NullPointerException::new).booleanValue(), b);
    }
    storage.delete();
  }

  @Test
  public static void testIntegerStorage() {
    Storage<Integer> storage = new LocalStorage<>(LocalStorageTest.class.getName(), "testIntStorage", Integer.class);
    for (int n : new int[] {1, -12}) {
      storage.save(n);
      Assert.assertEquals(Optional.ofNullable(storage.get()).orElseThrow(NullPointerException::new).intValue(), n);
    }
    storage.delete();
  }

  @Test
  public static void testNullStorage() {
    Storage<Double> storage = new LocalStorage<>(LocalStorageTest.class.getName(), "testNullStorage", Double.class);
    Assert.assertNull(storage.get());
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
    new LocalStorage<>(EMPTY, "testStringStorage", String.class);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public static void testNotUpdate() {
    new LocalStorage<>(LocalStorageTest.class.getName(), "testStringStorage", String.class).update(EMPTY);
  }

  @Test(expectedExceptions = CloneNotSupportedException.class)
  public static void testNotClone() throws CloneNotSupportedException {
    new LocalStorage<>(LocalStorageTest.class.getName(), "testStringStorage", String.class).clone();
  }

  @Test
  public void testInvalidFileName() {
    AtomicBoolean exceptionFlag = new AtomicBoolean(false);
    LOGGER.setFilter(record -> {
      Assert.assertNotNull(record.getThrown());
      exceptionFlag.set(true);
      return false;
    });
    new LocalStorage<>(LocalStorageTest.class.getName(), "/invalid file ...\\\\/", String.class).save(EMPTY);
    Assert.assertTrue(exceptionFlag.get(), "Exception must be thrown");
    LOGGER.setFilter(null);
  }
}
