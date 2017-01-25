package com.ak.rsm;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.nio.file.Paths;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import com.ak.util.LineFileCollector;
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
    Assert.assertNull(xVar.get().mapToObj(sToL -> String.format("%.2f", sToL)).collect(
        new LineFileCollector(Paths.get("x.txt"), LineFileCollector.Direction.HORIZONTAL)));

    Supplier<DoubleStream> yVar = () -> doubleRange(-0.98, 0.98).
        map(k12 -> -k12).filter(k12 -> Math.abs(k12) > PRECISION / 2);
    Assert.assertNull(yVar.get().mapToObj(k12 -> String.format("%.4f", ResistanceTwoLayer.getRho1ToRho2(k12))).collect(
        new LineFileCollector(Paths.get("y.txt"), LineFileCollector.Direction.VERTICAL)));

    Assert.assertNull(yVar.get().mapToObj(k12 -> String.format("%.2f", k12)).collect(
        new LineFileCollector(Paths.get("k12.txt"), LineFileCollector.Direction.VERTICAL)));
    return new Object[][] {{xVar, yVar}};
  }

  @DataProvider(name = "x = k12, s / L = {1 / 3, 1 / 2}")
  public static Object[][] k12() throws IOException {
    Supplier<DoubleStream> var = () -> DoubleStream.of(1.0 / 3.0, 1.0 / 2.0);
    Supplier<DoubleStream> xVar = () -> doubleRange(-0.98, 0.98).
        map(k12 -> -k12).filter(k12 -> Math.abs(k12) > PRECISION / 2);

    Assert.assertNull(xVar.get().mapToObj(k12 -> String.format("%.4f", ResistanceTwoLayer.getRho1ToRho2(k12))).collect(
        new LineFileCollector(Paths.get("x.txt"), LineFileCollector.Direction.VERTICAL)));

    Assert.assertNull(xVar.get().mapToObj(value -> String.format("%.2f", value)).collect(
        new LineFileCollector(Paths.get("k12.txt"), LineFileCollector.Direction.VERTICAL)));
    return new Object[][] {{var, xVar}};
  }

  @DataProvider(name = "x = s / L")
  public static Object[][] sToL() throws IOException {
    Supplier<DoubleStream> xVar = () -> doubleRange(0.01, 0.99);
    Assert.assertNull(xVar.get().mapToObj(sToL -> String.format("%.2f", sToL)).collect(
        new LineFileCollector(Paths.get("x.txt"), LineFileCollector.Direction.VERTICAL)));
    return new Object[][] {{xVar}};
  }

  @DataProvider(name = "x = k12, y = h / L")
  public static Object[][] k12byHtoL() throws IOException {
    Supplier<DoubleStream> xVar = () -> doubleRange(-0.98, 0.98).
        map(k12 -> -k12).filter(k12 -> Math.abs(k12) > PRECISION / 2);
    Assert.assertNull(xVar.get().mapToObj(k12 -> String.format("%.4f", ResistanceTwoLayer.getRho1ToRho2(k12))).collect(
        new LineFileCollector(Paths.get("x.txt"), LineFileCollector.Direction.HORIZONTAL)));
    Assert.assertNull(xVar.get().mapToObj(k12 -> String.format("%.2f", k12)).collect(
        new LineFileCollector(Paths.get("x-k12.txt"), LineFileCollector.Direction.HORIZONTAL)));

    Supplier<DoubleStream> yVar = () -> doubleRange(0.0, 0.5);
    Assert.assertNull(yVar.get().mapToObj(hToL -> String.format("%.2f", hToL)).collect(
        new LineFileCollector(Paths.get("y.txt"), LineFileCollector.Direction.VERTICAL)));
    return new Object[][] {{xVar, yVar}};
  }

  @DataProvider(name = "x = s / L, y = h / L")
  public static Object[][] sToLbyHtoL() throws IOException {
    Supplier<DoubleStream> xVar = () -> doubleRange(0.01, 0.99);
    Assert.assertNull(xVar.get().mapToObj(sToL -> String.format("%.2f", sToL)).collect(
        new LineFileCollector(Paths.get("x.txt"), LineFileCollector.Direction.HORIZONTAL)));

    Supplier<DoubleStream> yVar = () -> doubleRange(0.0, 0.5);
    Assert.assertNull(yVar.get().mapToObj(hToL -> String.format("%.2f", hToL)).collect(
        new LineFileCollector(Paths.get("y.txt"), LineFileCollector.Direction.VERTICAL)));
    return new Object[][] {{xVar, yVar}};
  }

  @Test(dataProvider = "x = s / L, y = k12", enabled = false)
  public static void testRho1SameRho2(Supplier<DoubleStream> xVar, Supplier<DoubleStream> yVar) throws IOException {
    Assert.assertNull(yVar.get().mapToObj(k12 -> xVar.get().map(sToL -> solve(new InequalityRbyRho2(k12, sToL), GoalType.MINIMIZE).getKey()[0])).
        map(stream -> stream.mapToObj(value -> String.format("%.6f", value)).collect(Collectors.joining("\t"))).
        collect(new LineFileCollector(Paths.get("z.txt"), LineFileCollector.Direction.VERTICAL)));
  }

  @Test(dataProvider = "x = k12, s / L = {1 / 3, 1 / 2}", enabled = false)
  public static void testRho1SameRho2SliceStoL(Supplier<DoubleStream> slice, Supplier<DoubleStream> xVar) throws IOException {
    Assert.assertNull(xVar.get().mapToObj(k12 -> slice.get().map(sToL -> solve(new InequalityRbyRho2(k12, sToL), GoalType.MINIMIZE).getKey()[0])).
        map(stream -> stream.mapToObj(value -> String.format("%.6f", value)).collect(Collectors.joining("\t"))).
        collect(new LineFileCollector(Paths.get("y.txt"), LineFileCollector.Direction.VERTICAL)));
  }

  @Test(dataProvider = "x = s / L", enabled = false)
  public static void testRho1SameRho2byRho(Supplier<DoubleStream> xVar) throws IOException {
    Assert.assertNull(xVar.get().mapToObj(sToL -> {
      PointValuePair pair = solve(new MaxInequalityRbyRho2(sToL), GoalType.MAXIMIZE);
      double hToL = pair.getValue();
      double rho12 = ResistanceTwoLayer.getRho1ToRho2(pair.getKey()[0]);
      return DoubleStream.of(hToL, rho12);
    }).map(stream -> stream.mapToObj(value -> String.format("%.6f", value)).collect(Collectors.joining("\t"))).
        collect(new LineFileCollector(Paths.get("y.txt"), LineFileCollector.Direction.VERTICAL)));
  }

  @Test(dataProvider = "x = k12, y = h / L", enabled = false)
  public static void testRho1SameRho2FixedStoL(Supplier<DoubleStream> xVar, Supplier<DoubleStream> yVar) throws IOException {
    Assert.assertNull(yVar.get().mapToObj(hToL -> xVar.get().
        map(k12 -> new DerivativeRbyRho2Normalized(k12, 1.0 / 2.0).value(hToL))).
        map(stream -> stream.mapToObj(value -> String.format("%.6f", value)).collect(Collectors.joining("\t"))).
        collect(new LineFileCollector(Paths.get("z.txt"), LineFileCollector.Direction.VERTICAL)));
  }

  @Test(dataProvider = "x = s / L, y = h / L", enabled = false)
  public static void testRho1SameRho2FixedRho12(Supplier<DoubleStream> xVar, Supplier<DoubleStream> yVar) throws IOException {
    Assert.assertNull(yVar.get().mapToObj(hToL -> xVar.get().
        map(sToL -> new DerivativeRbyRho2Normalized(ResistanceTwoLayer.getK12(3.0, 1.0), sToL).value(hToL))).
        map(stream -> stream.mapToObj(value -> String.format("%.6f", value)).collect(Collectors.joining("\t"))).
        collect(new LineFileCollector(Paths.get("z.txt"), LineFileCollector.Direction.VERTICAL)));
  }

  @Test(dataProvider = "x = s / L, y = k12", enabled = false)
  public static void testHPointMax(Supplier<DoubleStream> xVar, Supplier<DoubleStream> yVar) throws IOException {
    Assert.assertNull(yVar.get().mapToObj(k12 -> xVar.get().map(sToL -> solve(new InequalityRbyH(k12, sToL), GoalType.MAXIMIZE).getKey()[0])).
        map(stream -> stream.mapToObj(value -> String.format("%.6f", value)).collect(Collectors.joining("\t"))).
        collect(new LineFileCollector(Paths.get("z.txt"), LineFileCollector.Direction.VERTICAL)));
  }

  @Test(dataProvider = "x = s / L, y = k12", enabled = false)
  public static void testHValueMax(Supplier<DoubleStream> xVar, Supplier<DoubleStream> yVar) throws IOException {
    Assert.assertNull(yVar.get().mapToObj(k12 -> xVar.get().
        map(sToL -> -Math.signum(k12) * solve(new InequalityRbyH(k12, sToL), GoalType.MAXIMIZE).getValue())).
        map(stream -> stream.mapToObj(value -> String.format("%.6f", value)).collect(Collectors.joining("\t"))).
        collect(new LineFileCollector(Paths.get("z.txt"), LineFileCollector.Direction.VERTICAL)));
  }

  @Test(dataProvider = "x = k12, s / L = {1 / 3, 1 / 2}", enabled = false)
  public static void testHSlice(Supplier<DoubleStream> slice, Supplier<DoubleStream> xVar) throws IOException {
    Assert.assertNull(xVar.get().mapToObj(k12 -> slice.get().map(sToL -> solve(new InequalityRbyH(k12, sToL), GoalType.MAXIMIZE).getKey()[0])).
        map(stream -> stream.mapToObj(pointMax -> String.format("%.6f", pointMax)).collect(Collectors.joining("\t"))).
        collect(new LineFileCollector(Paths.get("y.txt"), LineFileCollector.Direction.VERTICAL)));
    Assert.assertNull(xVar.get().mapToObj(k12 -> slice.get().map(
        sToL -> -Math.signum(k12) * solve(new InequalityRbyH(k12, sToL), GoalType.MAXIMIZE).getValue())).
        map(stream -> stream.mapToObj(valueMax -> String.format("%.6f", valueMax)).collect(Collectors.joining("\t"))).
        collect(new LineFileCollector(Paths.get("y2.txt"), LineFileCollector.Direction.VERTICAL)));
  }

  @Test(dataProvider = "x = s / L", enabled = false)
  public static void testHbyRhoPointMax(Supplier<DoubleStream> xVar) throws IOException {
    Assert.assertNull(xVar.get().mapToObj(sToL -> {
      PointValuePair pair = solve(new MaxInequalityRbyH(sToL), GoalType.MAXIMIZE);
      double hToL = pair.getValue();
      double rho12 = ResistanceTwoLayer.getRho1ToRho2(pair.getKey()[0]);
      return DoubleStream.of(hToL, rho12);
    }).map(stream -> stream.mapToObj(value -> String.format("%.6f", value)).collect(Collectors.joining("\t"))).
        collect(new LineFileCollector(Paths.get("y.txt"), LineFileCollector.Direction.VERTICAL)));
  }

  @Test(dataProvider = "x = s / L", enabled = false)
  public static void testHValueMax(Supplier<DoubleStream> xVar) throws IOException {
    Assert.assertNull(xVar.get().mapToObj(sToL -> {
      PointValuePair pair = solve(new MaxInequalityRbyH(sToL), GoalType.MAXIMIZE);
      double k12 = pair.getKey()[0];
      double hToL = pair.getValue();
      double value = -Math.signum(k12) * new InequalityRbyH(k12, sToL).applyAsDouble(hToL);
      return DoubleStream.of(hToL, value);
    }).map(stream -> stream.mapToObj(value -> String.format("%.6f", value)).collect(Collectors.joining("\t"))).
        collect(new LineFileCollector(Paths.get("y.txt"), LineFileCollector.Direction.VERTICAL)));
  }

  @Test(dataProvider = "x = k12, y = h / L", enabled = false)
  public static void testHValueFixedStoL(Supplier<DoubleStream> xVar, Supplier<DoubleStream> yVar) throws IOException {
    Assert.assertNull(yVar.get().mapToObj(hToL -> xVar.get().
        map(k12 -> new DerivativeRbyHNormalized(k12, 1.0 / 2.0).value(hToL))).
        map(stream -> stream.mapToObj(value -> String.format("%.6f", value)).collect(Collectors.joining("\t"))).
        collect(new LineFileCollector(Paths.get("z.txt"), LineFileCollector.Direction.VERTICAL)));
  }

  @Test(dataProvider = "x = s / L, y = h / L", enabled = false)
  public static void testHValueFixedRho12(Supplier<DoubleStream> xVar, Supplier<DoubleStream> yVar) throws IOException {
    Assert.assertNull(yVar.get().mapToObj(hToL -> xVar.get().
        map(sToL -> new DerivativeRbyHNormalized(ResistanceTwoLayer.getK12(3.0, 1.0), sToL).value(hToL))).
        map(stream -> stream.mapToObj(value -> String.format("%.6f", value)).collect(Collectors.joining("\t"))).
        collect(new LineFileCollector(Paths.get("z.txt"), LineFileCollector.Direction.VERTICAL)));
  }

  @DataProvider(name = "x = h / L, y = L")
  public static Object[][] hL() throws IOException {
    Supplier<DoubleStream> xVarHL = () -> doubleRange(0.1, 0.9, 0.01);
    Assert.assertNull(xVarHL.get().mapToObj(value -> String.format("%.2f", value)).collect(
        new LineFileCollector(Paths.get("x.txt"), LineFileCollector.Direction.HORIZONTAL)));

    Supplier<DoubleStream> yVarL = () -> doubleRange(20.0, 120.0, 2.0);
    Assert.assertNull(yVarL.get().mapToObj(value -> String.format("%.2f", value)).collect(
        new LineFileCollector(Paths.get("y.txt"), LineFileCollector.Direction.VERTICAL)));
    return new Object[][] {{xVarHL, yVarL}};
  }

  @Test(dataProvider = "x = h / L, y = L", enabled = false)
  public static void testDerivativeRbyHDivideByRho(Supplier<DoubleStream> xVarHL, Supplier<DoubleStream> yVarL) {
    DoubleStream.of(0.5).forEachOrdered(sToL -> {
      try {
        Assert.assertNull(yVarL.get().
            peek(lmm -> Logger.getLogger(SoundingDepthTest.class.getName()).log(Level.INFO, String.format("L = %.2f", lmm))).
            mapToObj(lmm -> xVarHL.get().
                map(hToL -> {
                  double lMetre = lmm * 1.0e-3;
                  return StrictMath.log(new DerivativeRbyHDivideByRho(-1.0, sToL * lMetre, lMetre).value(hToL * lMetre));
                })
            ).map(stream -> stream.mapToObj(value -> String.format("%.6f", value)).collect(Collectors.joining("\t"))).
            collect(new LineFileCollector(Paths.get(String.format("dRdh_At_%.2f.txt", sToL)),
                LineFileCollector.Direction.VERTICAL)));
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
