package com.ak.fx.storage;

import java.awt.geom.Rectangle2D;

import org.testng.Assert;
import org.testng.annotations.Test;

public final class BoundsStorageTest {
  @Test
  public void testSave() {
    Storage<Rectangle2D.Double> storage = new BoundsStorage(BoundsStorageTest.class);
    Rectangle2D.Double rectangle = new Rectangle2D.Double(10, 20, 30, 40);
    storage.save(rectangle);
    Assert.assertEquals(storage.get(), rectangle);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testUpdate() {
    new BoundsStorage(BoundsStorageTest.class).update(new Rectangle2D.Double(10, 20, 30, 40));
  }
}