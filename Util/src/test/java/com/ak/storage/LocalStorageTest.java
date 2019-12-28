package com.ak.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import com.ak.util.LocalFileIO;
import com.ak.util.Strings;
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
  public static void testStringStorage() throws IOException {
    Storage<String> storage = new LocalStorage<>(LocalStorageTest.class.getName(), "testStringStorage", String.class);
    for (String s : new String[] {"name", "last"}) {
      storage.save(s);
      Assert.assertEquals(storage.get(), s);
    }

    Storage<String> storage2 = new LocalStorage<>(LocalStorageTest.class.getName(), "testStringStorage", String.class);
    Assert.assertEquals(storage2.get(), "last");
    LocalFileIO.AbstractBuilder BUILDER = new LocalStorageBuilder().addPath(LocalStorage.class.getSimpleName());
    Path testStringStorage = BUILDER.fileName(Strings.pointConcat(LocalStorageTest.class.getName(), "testStringStorage")).build().getPath();
    storage.delete();

    Files.createFile(testStringStorage);
    Assert.assertTrue(isSubstituteLogLevel(() -> {
      Storage<String> storage3 = new LocalStorage<>(LocalStorageTest.class.getName(), "testStringStorage", String.class);
      Assert.assertNull(storage3.get());
      storage3.delete();
    }, logRecord -> Assert.assertNotNull(logRecord.getMessage())));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public static void testEmptyFileName() {
    new LocalStorage<>(EMPTY, "testStringStorage", String.class);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public static void testNotUpdate() {
    new LocalStorage<>(LocalStorageTest.class.getName(), "testStringStorage", String.class).update(EMPTY);
  }

  @Test
  public static void testInvalidFileName() {
    Assert.assertTrue(isSubstituteLogLevel(
        () -> new LocalStorage<>(LocalStorageTest.class.getName(), "/invalid file ...\\\\/", String.class).save(EMPTY),
        logRecord -> {
          Assert.assertEquals(logRecord.getThrown().getClass(), NoSuchFileException.class);
          Assert.assertTrue(logRecord.getMessage().contains("/invalid file ...\\\\/"));
        })
    );
  }

  private static boolean isSubstituteLogLevel(Runnable runnable, Consumer<LogRecord> recordConsumer) {
    Level oldLevel = LOGGER.getLevel();
    LOGGER.setLevel(Level.WARNING);
    AtomicBoolean okFlag = new AtomicBoolean();
    LOGGER.setFilter(record -> {
      if (Objects.equals(record.getLevel(), Level.WARNING)) {
        recordConsumer.accept(record);
        okFlag.set(true);
      }
      return false;
    });
    runnable.run();
    LOGGER.setFilter(null);
    LOGGER.setLevel(oldLevel);
    return okFlag.get();
  }
}
