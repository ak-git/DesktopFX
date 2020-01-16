package com.ak.fx.storage;

import java.awt.geom.Rectangle2D;
import java.util.prefs.BackingStoreException;

import javax.annotation.Nonnull;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public final class BoundsStorageTest {
  @DataProvider(name = "storage")
  public static Object[][] storage() {
    return new Object[][] {
        {new Rectangle2D.Double(10, 20, 30, 40)},
    };
  }

  @Test(dataProvider = "storage")
  public void testSave(@Nonnull Rectangle2D.Double rectangle) throws BackingStoreException {
    Storage<Rectangle2D.Double> storage = new BoundsStorage(BoundsStorageTest.class);
    storage.save(rectangle);
    Assert.assertEquals(storage.get(), rectangle);
    storage.delete();
    Assert.assertNull(storage.get());
  }

  @Test(dataProvider = "storage", expectedExceptions = UnsupportedOperationException.class)
  public void testUpdate(Rectangle2D.Double rectangle) {
    new BoundsStorage(BoundsStorageTest.class).update(rectangle);
  }
}