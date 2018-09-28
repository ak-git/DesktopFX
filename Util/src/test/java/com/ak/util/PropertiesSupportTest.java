package com.ak.util;

import java.util.EnumSet;

import org.testng.Assert;
import org.testng.annotations.Test;


public class PropertiesSupportTest {
  private PropertiesSupportTest() {
  }

  @Test
  public void testCheck() {
    Assert.assertTrue(PropertiesSupport.CACHE.check());
    EnumSet.complementOf(EnumSet.of(PropertiesSupport.CACHE)).forEach(e -> {
      e.clear();
      Assert.assertFalse(e.check());
    });
  }

  @Test
  public void testValue() {
    Assert.assertEquals(PropertiesSupport.CACHE.value(), Boolean.toString(true));
    Assert.assertEquals(PropertiesSupport.OUT_CONVERTER_PATH.value(), OSDirectory.VENDOR_ID);
  }

  @Test
  public void testSet() {
    EnumSet.allOf(PropertiesSupport.class).forEach(e -> {
      e.set(e.name());
      Assert.assertEquals(e.value(), e.name());
      e.clear();
    });
  }

  @Test
  public void testKey() {
    EnumSet.allOf(PropertiesSupport.class).forEach(e -> Assert.assertEquals(e.key(), e.name().toLowerCase()));
  }
}