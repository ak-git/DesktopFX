package com.ak.util;

import java.lang.ref.Cleaner;
import java.util.concurrent.atomic.AtomicBoolean;

import org.testng.Assert;
import org.testng.annotations.Test;

public class CleanTest {
  @Test
  public void testClean() {
    boolean oldCache = PropertiesSupport.CACHE.check();
    PropertiesSupport.CACHE.update(Boolean.FALSE.toString());
    AtomicBoolean ok = new AtomicBoolean(false);
    Clean.clean(new Cleaner.Cleanable[] {
        () -> ok.set(true)
    });
    Assert.assertTrue(ok.get());
    PropertiesSupport.CACHE.update(Boolean.valueOf(oldCache).toString());
  }
}