package com.ak.util;

import java.util.EnumSet;

import org.testng.Assert;
import org.testng.annotations.Test;


public class PropertiesSupportTest {
  @Test
  public void testValue() {
    Assert.assertEquals(PropertiesSupport.OUT_CONVERTER_PATH.value(), OSDirectories.VENDOR_ID);
  }

  @Test
  public void testSet() {
    EnumSet.allOf(PropertiesSupport.class).forEach(e -> {
      e.update(e.name());
      Assert.assertEquals(e.value(), e.name());
      e.clear();
    });
  }

  @Test
  public void testKey() {
    EnumSet.allOf(PropertiesSupport.class).forEach(e -> Assert.assertEquals(e.key(), e.name().toLowerCase()));
  }
}