package com.ak.util;

import java.util.function.BooleanSupplier;
import java.util.logging.Filter;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class OSTest {
  private Filter oldFilter;

  private OSTest() {
  }

  @BeforeClass
  void setUp() {
    oldFilter = Logger.getLogger(OS.MAC.getClass().getName()).getFilter();
    Logger.getLogger(OS.MAC.getClass().getName()).setFilter(record -> {
      Assert.assertNotNull(record.getThrown());
      return false;
    });
  }

  @Test
  public void testGet() {
    Assert.assertEquals(Stream.of(OS.values()).filter(BooleanSupplier::getAsBoolean).count(), 1);
  }

  @Test
  public void testCallApplicationMethod() {
    Stream.of(OS.values()).forEach(os -> {
      try {
        os.callApplicationMethod("setDockIconImage", java.awt.Image.class, null);
        Assert.assertEquals(os, OS.MAC);
      }
      catch (Exception e) {
        Assert.assertNotNull(e.getMessage());
      }
    });
  }

  @Test
  public void testInvalidCallApplicationMethodMAC() {
    OS.MAC.callApplicationMethod("getDockIconImage", java.awt.Image.class, null);
  }

  @AfterClass
  public void tearDown() {
    Logger.getLogger(OS.MAC.getClass().getName()).setFilter(oldFilter);
  }
}