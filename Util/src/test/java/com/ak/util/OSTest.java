package com.ak.util;

import java.awt.image.BufferedImage;
import java.util.function.BooleanSupplier;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;

public class OSTest {
  private static final Logger LOGGER = Logger.getLogger(OS.MAC.getClass().getName());
  private boolean exceptionFlag;

  private OSTest() {
  }

  @BeforeClass
  void setUp() {
    LOGGER.setFilter(record -> {
      Assert.assertNotNull(record.getThrown());
      exceptionFlag = true;
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
        os.callApplicationMethod("setDockIconImage", java.awt.Image.class, new BufferedImage(1, 1, TYPE_INT_RGB));
        Assert.assertEquals(os, OS.MAC);
      }
      catch (Exception e) {
        Assert.assertNotNull(e.getMessage());
      }
    });
  }

  @Test
  public void testInvalidCallApplicationMethodMAC() {
    OS.MAC.callApplicationMethod("getDockIconImage", java.awt.Image.class, new BufferedImage(1, 1, TYPE_INT_RGB));
    Assert.assertTrue(exceptionFlag);
  }

  @AfterClass
  public void tearDown() {
    LOGGER.setFilter(null);
  }
}