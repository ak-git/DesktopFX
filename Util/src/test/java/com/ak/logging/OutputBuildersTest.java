package com.ak.logging;

import java.io.IOException;
import java.nio.file.Path;

import com.ak.util.Extension;
import org.testng.Assert;
import org.testng.annotations.Test;

public class OutputBuildersTest {
  @Test
  public void testOutputBuilders() throws IOException {
    String fileName = "02f29f660fa69e6c404c03de0f1e15f";
    Path path = OutputBuilders.build(fileName).getPath();
    Assert.assertTrue(path.toFile().getName().endsWith(Extension.CSV.name().toLowerCase()), path.toString());
  }
}