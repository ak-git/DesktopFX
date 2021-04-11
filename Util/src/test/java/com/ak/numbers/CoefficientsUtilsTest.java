package com.ak.numbers;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Scanner;

import com.ak.util.Extension;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CoefficientsUtilsTest {
  @Test
  public void testSerialize() {
    double[] out = CoefficientsUtils.serialize(new double[] {1.0, -1.0, 3.0, -3.0}, new double[] {1.0, -1.0}, 5);
    Assert.assertEquals(out, new double[] {1.0, 0.0, 3.0, 0.0, 0.0}, 1.0e-3);
  }

  @Test
  public void testRead() {
    InputStream resourceAsStream = getClass().getResourceAsStream(Extension.TXT.attachTo("DIFF"));
    Scanner scanner = new Scanner(resourceAsStream, Charset.defaultCharset().name());
    Assert.assertEquals(CoefficientsUtils.read(scanner), new double[] {-1.0, 0.0, 1.0});
  }
}