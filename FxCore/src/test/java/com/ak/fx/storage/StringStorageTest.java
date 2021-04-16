package com.ak.fx.storage;

import java.util.prefs.BackingStoreException;

import javax.annotation.Nonnull;

import com.ak.util.Strings;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public final class StringStorageTest {
  @DataProvider(name = "storage")
  public static Object[][] storage() {
    return new Object[][] {
        {"Something String"},
    };
  }

  private final Storage<String> storage = new StringStorage(StringStorageTest.class, "%08x".formatted(hashCode()));

  @Test(dataProvider = "storage")
  public void testSave(@Nonnull String value) throws BackingStoreException {
    storage.save(value);
    Assert.assertEquals(storage.get(), value);
    storage.delete();
    Assert.assertEquals(storage.get(), Strings.EMPTY);
  }

  @Test(dataProvider = "storage", expectedExceptions = UnsupportedOperationException.class)
  public void testUpdate(String value) {
    storage.update(value);
  }
}