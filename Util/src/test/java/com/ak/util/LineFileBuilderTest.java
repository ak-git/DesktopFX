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
        xRange(1.0, 3.0, 1.0).
        yLog10Range(10.0, 1.0).generate("z.txt", (x, y) -> x + y * 10);

    Path x = Paths.get("x.txt");
    Assert.assertEquals(Files.readAllLines(x, Charset.forName("windows-1251")).stream().collect(Collectors.joining()),
        "1\t2\t3");
    Assert.assertTrue(Files.deleteIfExists(x));

    Path y = Paths.get("y.txt");
    Assert.assertEquals(Files.readAllLines(y, Charset.forName("windows-1251")).stream().collect(Collectors.joining(Strings.SPACE)),
        "1.0 1.2 1.4 1.6 1.8 2.0 2.2 2.4 2.6 2.8 3.0 3.2 3.4 3.6 3.8 4.0 4.2 4.4 4.6 4.8 5.0 5.2 5.4 5.6 5.8 " +
            "6.0 6.2 6.4 6.6 6.8 7.0 7.2 7.4 7.6 7.8 8.0 8.2 8.4 8.6 8.8 9.0 9.2 9.4 9.6 9.8 10.0");
    Assert.assertTrue(Files.deleteIfExists(y));

    Path z = Paths.get("z.txt");
    Assert.assertEquals(Files.readAllLines(z, Charset.forName("windows-1251")).stream().collect(Collectors.joining(Strings.TAB)),
        "11\t12\t13\t13\t14\t15\t15\t16\t17\t17\t18\t19\t19\t20\t21\t21\t22\t23\t23\t24\t25\t25\t26\t27\t27\t28\t29\t29\t" +
            "30\t31\t31\t32\t33\t33\t34\t35\t35\t36\t37\t37\t38\t39\t39\t40\t41\t41\t42\t43\t43\t44\t45\t45\t46\t47\t47\t48\t49\t49\t" +
            "50\t51\t51\t52\t53\t53\t54\t55\t55\t56\t57\t57\t58\t59\t59\t60\t61\t61\t62\t63\t63\t64\t65\t65\t66\t67\t67\t68\t69\t69\t" +
            "70\t71\t71\t72\t73\t73\t74\t75\t75\t76\t77\t77\t78\t79\t79\t80\t81\t81\t82\t83\t83\t84\t85\t85\t86\t87\t87\t88\t89\t89\t" +
            "90\t91\t91\t92\t93\t93\t94\t95\t95\t96\t97\t97\t98\t99\t99\t100\t101\t101\t102\t103");
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