package com.ak.util;

import java.util.function.BooleanSupplier;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class OSTest {
  private static final Logger LOGGER = Logger.getLogger(OS.MAC.getClass().getName());

  private OSTest() {
  }

  @BeforeClass
  void setUp() {
    LOGGER.setFilter(record -> {
      Assert.assertNotNull(record.getThrown());
      return false;
    });
  }

  @Test
  public static void testGet() {
    Assert.assertEquals(Stream.of(OS.values()).filter(BooleanSupplier::getAsBoolean).count(), 1);
  }

  @AfterClass
  public static void tearDown() {
    LOGGER.setFilter(null);
  }
}