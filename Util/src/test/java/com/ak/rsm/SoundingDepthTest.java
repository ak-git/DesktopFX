package com.ak.rsm;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.nio.file.Paths;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import com.ak.util.LineFileBuilder;
import com.ak.util.LineFileCollector;
import com.ak.util.Strings;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.NelderMeadSimplex;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizer;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class SoundingDepthTest {
  private static final double PRECISION = 1.0e-2;

  private SoundingDepthTest() {
  }

  /**
   * Finds point where <b>dR/dRho1 * Rho1 / R == dR/dRho2 * Rho2 / R == 1 / 2</b>
   */
  private static class InequalityRbyRho2 implements DoubleUnaryOperator {
    private final UnivariateFunction dRbyRho2N;

    private InequalityRbyRho2(double k12, double sToL) {
      dRbyRho2N = new DerivativeRbyRho2Normalized(k12, sToL);
    }

    @Override
    public double applyAsDouble(double hToL) {
      return Math.abs(dRbyRho2N.value(Math.max(0, hToL)) - 0.5);
    }
  }

  /**
   * Finds <b>k12 = f(s/L)</b> where <b>h/L = g(dR/dRho2 normalized by Rho2 / R)</b> reaches maximum.
   */
  private static class MaxInequalityRbyRho2 implements DoubleUnaryOperator {
    private final double sToL;

    private MaxInequalityRbyRho2(double sToL) {
      this.sToL = sToL;
    }

    @Override
    public double applyAsDouble(double k12) {
      return solve(new InequalityRbyRho2(k12, sToL), GoalType.MINIMIZE).getKey()[0];
    }
  }

  /**
   * Finds point <b>h/L</b> where <b>dR/dh * h / L</b> reaches maximum
   */
  private static class InequalityRbyH implements DoubleUnaryOperator {
    private final UnivariateFunction dRbyH;

    private InequalityRbyH(double k12, double sToL) {
      dRbyH = new DerivativeRbyHNormalized(k12, sToL);
    }

    @Override
    public double applyAsDouble(double hToL) {
      return Math.abs(dRbyH.value(Math.max(0, hToL)));
    }
  }

  /**
   * Finds <b>k12 = f(s/L)</b> where <b>h/L = g(dR/dh normalized by L / R)</b> reaches maximum.
   */
  private static class MaxInequalityRbyH implements DoubleUnaryOperator {
    private final double sToL;

    private MaxInequalityRbyH(double sToL) {
      this.sToL = sToL;
    }

    @Override
    public double applyAsDouble(double k12) {
      if (k12 > -0.001) {
        return 0.0;
      }
      else {
        return solve(new InequalityRbyH(k12, sToL), GoalType.MAXIMIZE).getKey()[0];
      }
    }
  }

  private static PointValuePair solve(DoubleUnaryOperator inequality, GoalType goalType) {
    SimplexOptimizer optimizer = new SimplexOptimizer(-1, 1.0e-6);
    return optimizer.optimize(new MaxEval(100), new ObjectiveFunction(
            x -> inequality.applyAsDouble(x[0])), goalType,
        new NelderMeadSimplex(1, 0.01),
        new InitialGuess(new double[] {0.0})
    );
  }

  @DataProvider(name = "x = s / L, y = k12")
  public static Object[][] sToLbyK12() throws IOException {
    Supplier<DoubleStream> xVar = () -> doubleRange(0.01, 0.99);
    Assert.assertTrue(xVar.get().mapToObj(sToL -> String.format("%.2f", sToL)).collect(
        new LineFileCollector(Paths.get("x.txt"), LineFileCollector.Direction.HORIZONTAL)));

    Supplier<DoubleStream> yVar = () -> doubleRange(-0.98, 0.98).
        map(k12 -> -k12).filter(k12 -> Math.abs(k12) > PRECISION / 2);
    Assert.assertTrue(yVar.get().mapToObj(k12 -> String.format("%.4f", ResistanceTwoLayer.getRho1ToRho2(k12))).collect(
        new LineFileCollector(Paths.get("y.txt"), LineFileCollector.Direction.VERTICAL)));

    Assert.assertTrue(yVar.get().mapToObj(k12 -> String.format("%.2f", k12)).collect(
        new LineFileCollector(Paths.get("k12.txt"), LineFileCollector.Direction.VERTICAL)));
    return new Object[][] {{xVar, yVar}};
  }

  @DataProvider(name = "x = k12, s / L = {1 / 3, 1 / 2}")
  public static Object[][] k12() throws IOException {
    Supplier<DoubleStream> var = () -> DoubleStream.of(1.0 / 3.0, 1.0 / 2.0);
    Supplier<DoubleStream> xVar = () -> doubleRange(-0.98, 0.98).
        map(k12 -> -k12).filter(k12 -> Math.abs(k12) > PRECISION / 2);

    Assert.assertTrue(xVar.get().mapToObj(k12 -> String.format("%.4f", ResistanceTwoLayer.getRho1ToRho2(k12))).collect(
        new LineFileCollector(Paths.get("x.txt"), LineFileCollector.Direction.VERTICAL)));

    Assert.assertTrue(xVar.get().mapToObj(value -> String.format("%.2f", value)).collect(
        new LineFileCollector(Paths.get("k12.txt"), LineFileCollector.Direction.VERTICAL)));
    return new Object[][] {{var, xVar}};
  }

  @DataProvider(name = "x = s / L")
  public static Object[][] sToL() throws IOException {
    Supplier<DoubleStream> xVar = () -> doubleRange(0.01, 0.99);
    Assert.assertTrue(xVar.get().mapToObj(sToL -> String.format("%.2f", sToL)).collect(
        new LineFileCollector(Paths.get("x.txt"), LineFileCollector.Direction.VERTICAL)));
    return new Object[][] {{xVar}};
  }

  @DataProvider(name = "x = k12, y = h / L")
  public static Object[][] k12byHtoL() throws IOException {
    Supplier<DoubleStream> xVar = () -> doubleRange(-0.98, 0.98).
        map(k12 -> -k12).filter(k12 -> Math.abs(k12) > PRECISION / 2);
    Assert.assertTrue(xVar.get().mapToObj(k12 -> String.format("%.4f", ResistanceTwoLayer.getRho1ToRho2(k12))).collect(
        new LineFileCollector(Paths.get("x.txt"), LineFileCollector.Direction.HORIZONTAL)));
    Assert.assertTrue(xVar.get().mapToObj(k12 -> String.format("%.2f", k12)).collect(
        new LineFileCollector(Paths.get("x-k12.txt"), LineFileCollector.Direction.HORIZONTAL)));

    Supplier<DoubleStream> yVar = () -> doubleRange(0.0, 0.5);
    Assert.assertTrue(yVar.get().mapToObj(hToL -> String.format("%.2f", hToL)).collect(
        new LineFileCollector(Paths.get("y.txt"), LineFileCollector.Direction.VERTICAL)));
    return new Object[][] {{xVar, yVar}};
  }

  @Test(dataProvider = "x = s / L, y = k12", enabled = false)
  public static void testRho1SameRho2(Supplier<DoubleStream> xVar, Supplier<DoubleStream> yVar) throws IOException {
    LineFileBuilder.of("%.4f %.2f %.6f").xStream(xVar).yStream(yVar).
        generate("z.txt", (sToL, k12) -> solve(new InequalityRbyRho2(k12, sToL), GoalType.MINIMIZE).getKey()[0]);
  }

  @Test(dataProvider = "x = k12, s / L = {1 / 3, 1 / 2}", enabled = false)
  public static void testRho1SameRho2SliceStoL(Supplier<DoubleStream> slice, Supplier<DoubleStream> xVar) throws IOException {
    LineFileBuilder.<PointValuePair>of("%.4f %.4f %.6f").xStream(slice).yStream(xVar).
        generate("y.txt", (sToL, k12) -> solve(new InequalityRbyRho2(k12, sToL), GoalType.MINIMIZE).getKey()[0]);
  }

  @Test(dataProvider = "x = s / L", enabled = false)
  public static void testRho1SameRho2byRho(Supplier<DoubleStream> xVar) throws IOException {
    Assert.assertTrue(xVar.get().mapToObj(sToL -> {
      PointValuePair pair = solve(new MaxInequalityRbyRho2(sToL), GoalType.MAXIMIZE);
      double hToL = pair.getValue();
      double rho12 = ResistanceTwoLayer.getRho1ToRho2(pair.getKey()[0]);
      return DoubleStream.of(hToL, rho12);
    }).map(stream -> stream.mapToObj(value -> String.format("%.6f", value)).collect(Collectors.joining(Strings.TAB))).
        collect(new LineFileCollector(Paths.get("y.txt"), LineFileCollector.Direction.VERTICAL)));
  }

  @Test(dataProvider = "x = k12, y = h / L", enabled = false)
  public static void testRho1SameRho2FixedStoL(Supplier<DoubleStream> xVar, Supplier<DoubleStream> yVar) throws IOException {
    LineFileBuilder.of("%.4f %.2f %.6f").xStream(xVar).yStream(yVar).
        generate("z.txt", (k12, hToL) -> new DerivativeRbyRho2Normalized(k12, 1.0 / 2.0).value(hToL));
  }

  @Test(enabled = false)
  public static void testRho1SameRho2FixedRho12() throws IOException {
    LineFileBuilder.of("%.2f %.2f %.6f").
        xRange(0.01, 0.99, PRECISION).
        yRange(0.0, 0.5, PRECISION).
        generate("z.txt", (sToL, hToL) ->
            new DerivativeRbyRho2Normalized(ResistanceTwoLayer.getK12(3.0, 1.0), sToL).value(hToL)
        );
  }

  @Test(dataProvider = "x = s / L, y = k12", enabled = false)
  public static void testHPointMax(Supplier<DoubleStream> xVar, Supplier<DoubleStream> yVar) throws IOException {
    LineFileBuilder.of("%.2f %.4f %.6f").xStream(xVar).yStream(yVar).
        generate("z.txt", (sToL, k12) -> solve(new InequalityRbyH(k12, sToL), GoalType.MAXIMIZE).getKey()[0]);
  }

  @Test(dataProvider = "x = s / L, y = k12", enabled = false)
  public static void testHValueMax(Supplier<DoubleStream> xVar, Supplier<DoubleStream> yVar) throws IOException {
    LineFileBuilder.of("%.2f %.4f %.6f").xStream(xVar).yStream(yVar).
        generate("z.txt", (sToL, k12) -> -Math.signum(k12) * solve(new InequalityRbyH(k12, sToL), GoalType.MAXIMIZE).getValue());
  }

  @Test(dataProvider = "x = k12, s / L = {1 / 3, 1 / 2}", enabled = false)
  public static void testHSlice(Supplier<DoubleStream> slice, Supplier<DoubleStream> xVar) throws IOException {
    LineFileBuilder.<PointValuePair>of("%.4f %.4f %.6f").xStream(slice).yStream(xVar).
        generate("y.txt", (sToL, k12) -> solve(new InequalityRbyH(k12, sToL), GoalType.MAXIMIZE).getKey()[0]);

    LineFileBuilder.<PointValuePair>of("%.4f %.4f %.6f").xStream(slice).yStream(xVar).
        generate("y2.txt", (sToL, k12) -> -Math.signum(k12) * solve(new InequalityRbyH(k12, sToL), GoalType.MAXIMIZE).getValue());
  }

  @Test(dataProvider = "x = s / L", enabled = false)
  public static void testHbyRhoPointMax(Supplier<DoubleStream> xVar) throws IOException {
    Assert.assertTrue(xVar.get().mapToObj(sToL -> {
      PointValuePair pair = solve(new MaxInequalityRbyH(sToL), GoalType.MAXIMIZE);
      double hToL = pair.getValue();
      double rho12 = ResistanceTwoLayer.getRho1ToRho2(pair.getKey()[0]);
      return DoubleStream.of(hToL, rho12);
    }).map(stream -> stream.mapToObj(value -> String.format("%.6f", value)).collect(Collectors.joining(Strings.TAB))).
        collect(new LineFileCollector(Paths.get("y.txt"), LineFileCollector.Direction.VERTICAL)));
  }

  @Test(dataProvider = "x = s / L", enabled = false)
  public static void testHValueMax(Supplier<DoubleStream> xVar) throws IOException {
    Assert.assertTrue(xVar.get().mapToObj(sToL -> {
      PointValuePair pair = solve(new MaxInequalityRbyH(sToL), GoalType.MAXIMIZE);
      double k12 = pair.getKey()[0];
      double hToL = pair.getValue();
      double value = -Math.signum(k12) * new InequalityRbyH(k12, sToL).applyAsDouble(hToL);
      return DoubleStream.of(hToL, value);
    }).map(stream -> stream.mapToObj(value -> String.format("%.6f", value)).collect(Collectors.joining(Strings.TAB))).
        collect(new LineFileCollector(Paths.get("y.txt"), LineFileCollector.Direction.VERTICAL)));
  }

  @Test(dataProvider = "x = k12, y = h / L", enabled = false)
  public static void testHValueFixedStoL(Supplier<DoubleStream> xVar, Supplier<DoubleStream> yVar) throws IOException {
    LineFileBuilder.of("%.4f %.2f %.6f").xStream(xVar).yStream(yVar).
        generate("z.txt", (k12, hToL) -> new DerivativeRbyHNormalized(k12, 1.0 / 2.0).value(hToL));
  }

  @Test(enabled = false)
  public static void testHValueFixedRho12() throws IOException {
    LineFileBuilder.of("%.2f %.2f %.6f").
        xRange(0.01, 0.99, PRECISION).
        yRange(0.0, 0.5, PRECISION).
        generate("z.txt", (sToL, hToL) ->
            new DerivativeRbyHNormalized(ResistanceTwoLayer.getK12(3.0, 1.0), sToL).value(hToL)
        );
  }

  @Test(enabled = false)
  public static void testDerivativeRbyHDivideByRho() {
    DoubleStream.of(1.0 / 3.0, 0.5).forEachOrdered(sToL -> {
      try {
        LineFileBuilder.of("%.2f %.2f %.6f").
            xRange(0.1, 0.9, 0.01).
            yRange(20.0, 120.0, 1.0).
            generate(String.format("dRdh_At_%.2f.txt", sToL),
                (hToL, lmm) -> {
                  double lMetre = lmm * 1.0e-3;
                  return StrictMath.log10(new DerivativeRbyHDivideByRho(-1.0, sToL * lMetre, lMetre).value(hToL * lMetre));
                });
      }
      catch (IOException e) {
        Assert.fail(e.getMessage(), e);
      }
    });
  }

  @Test(enabled = false)
  public static void testDerivativeRbyHDivideByRhoAbsolute() {
    DoubleStream.of(1.0 / 3.0, 0.5).forEachOrdered(sToL -> {
      try {
        LineFileBuilder.of("%.2f %.2f %.6f").
            xRange(20.0, 120.0, 1.0).
            yRange(4.0, 30.0, 0.1).
            generate(String.format("dRdh_At_%.2f.txt", sToL),
                (lmm, hmm) -> {
                  double lMetre = lmm * 1.0e-3;
                  return StrictMath.log10(new DerivativeRbyHDivideByRho(-1.0, sToL * lMetre, lMetre).value(hmm * 1.0e-3));
                });
      }
      catch (IOException e) {
        Assert.fail(e.getMessage(), e);
      }
    });
  }

  private static DoubleStream doubleRange(double start, double end) {
    return doubleRange(start, end, PRECISION);
  }

  private static DoubleStream doubleRange(double start, double end, double precision) {
    return DoubleStream.iterate(start, dl2L -> dl2L + precision).
        limit(BigDecimal.valueOf((end - start) / precision + 1).round(MathContext.UNLIMITED).intValue()).sequential();
  }
}
