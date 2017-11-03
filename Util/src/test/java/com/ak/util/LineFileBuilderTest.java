package com.ak.util;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import org.testng.Assert;
import org.testng.annotations.Test;

public class LineFileBuilderTest {
  private LineFileBuilderTest() {
  }

  @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "1 invalid")
  public static void testOf() {
    LineFileBuilder.of("%.4f %.2f %.1f");
    LineFileBuilder.of("1 invalid");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public static void testXRange() {
    LineFileBuilder.of("%.2f %.2f %.1f").xRange(2.0, 1.0, 0.1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public static void testYRange() {
    LineFileBuilder.of("%.2f %.2f %.1f").yRange(1.0, 10.0, 10.0);
  }

  @Test
  public static void testGenerateRange() throws IOException {
    LineFileBuilder.of("%.0f %.0f %.0f").
        xRange(1.0, 3.0, 1.0).
        yRange(1.0, 2.0, 1.0).generate("z.txt", (x, y) -> x + y * 10);

    Path x = Paths.get("x.txt");
    Assert.assertEquals(Files.readAllLines(x, Charset.forName("windows-1251")).stream().collect(Collectors.joining()),
        "1\t2\t3");
    Assert.assertTrue(Files.deleteIfExists(x));

    Path y = Paths.get("y.txt");
    Assert.assertEquals(Files.readAllLines(y, Charset.forName("windows-1251")).stream().collect(Collectors.joining(Strings.SPACE)),
        "1 2");
    Assert.assertTrue(Files.deleteIfExists(y));

    Path z = Paths.get("z.txt");
    Assert.assertEquals(Files.readAllLines(z, Charset.forName("windows-1251")).stream().collect(Collectors.joining(Strings.TAB)),
        "11\t12\t13\t21\t22\t23");
    Assert.assertTrue(Files.deleteIfExists(z));
  }

  @Test
  public static void testGenerateLogRange() throws IOException {
    LineFileBuilder.of("%.0f %.1f %.0f").
        xLog10Range(10.0, 20.0).
        yLog10Range(10.0, 1.0).generate("z.txt", (x, y) -> x + y * 10);

    Path x = Paths.get("x.txt");
    Assert.assertEquals(Files.readAllLines(x, Charset.forName("windows-1251")).stream().collect(Collectors.joining()),
        "10\t12\t14\t16\t18\t20");
    Assert.assertTrue(Files.deleteIfExists(x));

    Path y = Paths.get("y.txt");
    Assert.assertEquals(Files.readAllLines(y, Charset.forName("windows-1251")).stream().collect(Collectors.joining(Strings.SPACE)),
        "1.0 1.2 1.4 1.6 1.8 2.0 2.2 2.4 2.6 2.8 3.0 3.2 3.4 3.6 3.8 4.0 4.2 4.4 4.6 4.8 5.0 5.2 5.4 5.6 5.8 " +
            "6.0 6.2 6.4 6.6 6.8 7.0 7.2 7.4 7.6 7.8 8.0 8.2 8.4 8.6 8.8 9.0 9.2 9.4 9.6 9.8 10.0");
    Assert.assertTrue(Files.deleteIfExists(y));

    Path z = Paths.get("z.txt");
    Assert.assertEquals(Files.readAllLines(z, Charset.forName("windows-1251")).stream().collect(Collectors.joining(Strings.TAB)),
        "20\t22\t24\t26\t28\t30\t22\t24\t26\t28\t30\t32\t24\t26\t28\t" +
            "30\t32\t34\t26\t28\t30\t32\t34\t36\t28\t30\t32\t34\t36\t38\t" +
            "30\t32\t34\t36\t38\t40\t32\t34\t36\t38\t40\t42\t34\t36\t38\t" +
            "40\t42\t44\t36\t38\t40\t42\t44\t46\t38\t40\t42\t44\t46\t48\t" +
            "40\t42\t44\t46\t48\t50\t42\t44\t46\t48\t50\t52\t44\t46\t48\t" +
            "50\t52\t54\t46\t48\t50\t52\t54\t56\t48\t50\t52\t54\t56\t58\t" +
            "50\t52\t54\t56\t58\t60\t52\t54\t56\t58\t60\t62\t54\t56\t58\t" +
            "60\t62\t64\t56\t58\t60\t62\t64\t66\t58\t60\t62\t64\t66\t68\t" +
            "60\t62\t64\t66\t68\t70\t62\t64\t66\t68\t70\t72\t64\t66\t68\t" +
            "70\t72\t74\t66\t68\t70\t72\t74\t76\t68\t70\t72\t74\t76\t78\t" +
            "70\t72\t74\t76\t78\t80\t72\t74\t76\t78\t80\t82\t74\t76\t78\t" +
            "80\t82\t84\t76\t78\t80\t82\t84\t86\t78\t80\t82\t84\t86\t88\t" +
            "80\t82\t84\t86\t88\t90\t82\t84\t86\t88\t90\t92\t84\t86\t88\t" +
            "90\t92\t94\t86\t88\t90\t92\t94\t96\t88\t90\t92\t94\t96\t98\t" +
            "90\t92\t94\t96\t98\t100\t92\t94\t96\t98\t100\t102\t94\t96\t98\t" +
            "100\t102\t104\t96\t98\t100\t102\t104\t106\t98\t100\t102\t104\t106\t108\t" +
            "100\t102\t104\t106\t108\t110\t102\t104\t106\t108\t110\t112\t104\t106\t108\t" +
            "110\t112\t114\t106\t108\t110\t112\t114\t116\t108\t110\t112\t114\t116\t118\t110" +
            "\t112\t114\t116\t118\t120");
    Assert.assertTrue(Files.deleteIfExists(z));
  }

  @Test
  public static void testGenerateStream() throws IOException {
    LineFileBuilder.of("%.0f %.0f %.0f").
        xStream(() -> DoubleStream.of(1.0, 2.0)).
        yStream(() -> DoubleStream.of(1.0, 2.0)).generate("z.txt", (x, y) -> x + y * 2.0);

    Assert.assertTrue(Files.notExists(Paths.get("x.txt")));
    Assert.assertTrue(Files.notExists(Paths.get("y.txt")));

    Path z = Paths.get("z.txt");
    Assert.assertEquals(Files.readAllLines(z, Charset.forName("windows-1251")).stream().collect(Collectors.joining(Strings.TAB)),
        "3\t4\t5\t6");
    Assert.assertTrue(Files.deleteIfExists(z));
  }
}