package com.ak.rsm;

import java.math.BigDecimal;
import java.math.MathContext;
import java.nio.file.Paths;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Supplier;
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
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public final class SoundingDepthTest {
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
   * Finds <b>k12 = f(s/L)</b> where <b>h/L = g(dR/dRho2 normalized)</b> reaches maximum.
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
      dRbyH = new DerivativeRbyHNormalizedByL(k12, sToL);
    }

    @Override
    public double applyAsDouble(double hToL) {
      return Math.abs(dRbyH.value(Math.max(0, hToL)));
    }
  }

  /**
   * Finds <b>k12 = f(s/L)</b> where <b>h/L = g(dR/dh normalized by L)</b> reaches maximum.
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
  public static Object[][] sToLbyK12() {
    Supplier<DoubleStream> xVar = () -> doubleRange(0.01, 0.99, 1.0e-2);
    xVar.get().mapToObj(value -> String.format("%.2f", value)).collect(
        new LineFileCollector<>(Paths.get("x.txt"), LineFileCollector.Direction.HORIZONTAL));

    Supplier<DoubleStream> yVar = () -> doubleRange(-0.99, 0.99, 1.0e-2).filter(k12 -> Math.abs(k12) > 1.0e-2 / 2);
    yVar.get().mapToObj(value -> String.format("%.2f", value)).collect(
        new LineFileCollector<>(Paths.get("y.txt"), LineFileCollector.Direction.VERTICAL));
    return new Object[][] {{xVar, yVar}};
  }

  @DataProvider(name = "x = s / L, y = rho1 / rho2")
  public static Object[][] sToLbyRho1Rho2() {
    Supplier<DoubleStream> xVar = () -> doubleRange(0.01, 0.99, 1.0e-2);
    xVar.get().mapToObj(value -> String.format("%.2f", value)).collect(
        new LineFileCollector<>(Paths.get("x.txt"), LineFileCollector.Direction.HORIZONTAL));

    Supplier<DoubleStream> yVar = () -> log10DoubleRange(1.0e-2, 1.0e2, 16.0).filter(rho12 -> Math.abs(rho12 - 1.0) > 1.0e-2 / 2);
    yVar.get().mapToObj(value -> String.format("%.4f", value)).collect(
        new LineFileCollector<>(Paths.get("y.txt"), LineFileCollector.Direction.VERTICAL));
    return new Object[][] {{xVar, yVar}};
  }

  @DataProvider(name = "x = s / L")
  public static Object[][] sToL() {
    Supplier<DoubleStream> xVar = () -> doubleRange(0.01, 0.99, 1.0e-2);
    xVar.get().mapToObj(value -> String.format("%.2f", value)).collect(
        new LineFileCollector<>(Paths.get("x.txt"), LineFileCollector.Direction.VERTICAL));
    return new Object[][] {{xVar}};
  }

  @Test(dataProvider = "x = s / L, y = k12", enabled = false)
  public void testRho1SameRho2byK12(Supplier<DoubleStream> xVar, Supplier<DoubleStream> yVar) {
    yVar.get().mapToObj(k12 -> xVar.get().map(sToL -> solve(new InequalityRbyRho2(k12, sToL), GoalType.MINIMIZE).getKey()[0])).
        map(stream -> stream.mapToObj(value -> String.format("%.6f", value)).collect(Collectors.joining("\t"))).
        collect(new LineFileCollector<>(Paths.get("z.txt"), LineFileCollector.Direction.VERTICAL));
  }

  @Test(dataProvider = "x = s / L, y = rho1 / rho2", enabled = false)
  public void testRho1SameRho2byRho(Supplier<DoubleStream> xVar, Supplier<DoubleStream> yVar) {
    yVar.get().mapToObj(rhoToRho -> xVar.get().map(sToL -> solve(
        new InequalityRbyRho2(ResistanceTwoLayer.getK12(rhoToRho, 1.0), sToL), GoalType.MINIMIZE).getKey()[0])).
        map(stream -> stream.mapToObj(value -> String.format("%.6f", value)).collect(Collectors.joining("\t"))).
        collect(new LineFileCollector<>(Paths.get("z.txt"), LineFileCollector.Direction.VERTICAL));
  }

  @Test(dataProvider = "x = s / L", enabled = false)
  public void testRho1SameRho2byRho(Supplier<DoubleStream> xVar) {
    xVar.get().mapToObj(sToL -> {
      PointValuePair pair = solve(new MaxInequalityRbyRho2(sToL), GoalType.MAXIMIZE);
      double rho12 = ResistanceTwoLayer.getRho1ToRho2(pair.getKey()[0]);
      double h = pair.getValue();
      return DoubleStream.of(rho12, h);
    }).map(stream -> stream.mapToObj(value -> String.format("%.6f", value)).collect(Collectors.joining("\t"))).
        collect(new LineFileCollector<>(Paths.get("y.txt"), LineFileCollector.Direction.VERTICAL));
  }

  @Test(dataProvider = "x = s / L, y = k12", enabled = false)
  public void testHLbyK12PointMax(Supplier<DoubleStream> xVar, Supplier<DoubleStream> yVar) {
    yVar.get().mapToObj(k12 -> xVar.get().map(sToL -> solve(new InequalityRbyH(k12, sToL), GoalType.MAXIMIZE).getKey()[0])).
        map(stream -> stream.mapToObj(value -> String.format("%.6f", value)).collect(Collectors.joining("\t"))).
        collect(new LineFileCollector<>(Paths.get("z.txt"), LineFileCollector.Direction.VERTICAL));
  }

  @Test(dataProvider = "x = s / L, y = k12", enabled = false)
  public void testHLbyK12ValueMax(Supplier<DoubleStream> xVar, Supplier<DoubleStream> yVar) {
    yVar.get().mapToObj(k12 -> xVar.get().
        map(sToL -> -Math.signum(k12) * solve(new InequalityRbyH(k12, sToL), GoalType.MAXIMIZE).getValue())).
        map(stream -> stream.mapToObj(value -> String.format("%.6f", value)).collect(Collectors.joining("\t"))).
        collect(new LineFileCollector<>(Paths.get("z.txt"), LineFileCollector.Direction.VERTICAL));
  }

  @Test(dataProvider = "x = s / L, y = rho1 / rho2", enabled = false)
  public void testHLbyRhoPointMax(Supplier<DoubleStream> xVar, Supplier<DoubleStream> yVar) {
    yVar.get().mapToObj(rhoToRho -> xVar.get().map(sToL -> solve(
        new InequalityRbyH(ResistanceTwoLayer.getK12(rhoToRho, 1.0), sToL), GoalType.MAXIMIZE).getKey()[0])).
        map(stream -> stream.mapToObj(value -> String.format("%.6f", value)).collect(Collectors.joining("\t"))).
        collect(new LineFileCollector<>(Paths.get("z.txt"), LineFileCollector.Direction.VERTICAL));
  }

  @Test(dataProvider = "x = s / L, y = rho1 / rho2", enabled = false)
  public void testHLbyRhoValueMax(Supplier<DoubleStream> xVar, Supplier<DoubleStream> yVar) {
    yVar.get().mapToObj(rhoToRho -> xVar.get().
        map(sToL -> -Math.signum(ResistanceTwoLayer.getK12(rhoToRho, 1.0)) *
            solve(new InequalityRbyH(ResistanceTwoLayer.getK12(rhoToRho, 1.0), sToL), GoalType.MAXIMIZE).getValue())).
        map(stream -> stream.mapToObj(value -> String.format("%.6f", value)).collect(Collectors.joining("\t"))).
        collect(new LineFileCollector<>(Paths.get("z.txt"), LineFileCollector.Direction.VERTICAL));
  }

  @Test(dataProvider = "x = s / L", enabled = false)
  public void testHLbyRhoPointMax(Supplier<DoubleStream> xVar) {
    xVar.get().mapToObj(sToL -> {
      PointValuePair pair = solve(new MaxInequalityRbyH(sToL), GoalType.MAXIMIZE);
      double rho12 = ResistanceTwoLayer.getRho1ToRho2(pair.getKey()[0]);
      double h = pair.getValue();
      return DoubleStream.of(rho12, h);
    }).map(stream -> stream.mapToObj(value -> String.format("%.6f", value)).collect(Collectors.joining("\t"))).
        collect(new LineFileCollector<>(Paths.get("y.txt"), LineFileCollector.Direction.VERTICAL));
  }

  private static DoubleStream doubleRange(double start, double end, double step) {
    return DoubleStream.iterate(start, dl2L -> dl2L + step).
        limit(BigDecimal.valueOf((end - start) / step + 1).round(MathContext.UNLIMITED).intValue()).sequential();
  }

  private static DoubleStream log10DoubleRange(double start, double end, double invPowOf10) {
    return DoubleStream.iterate(start, dl2L -> dl2L * StrictMath.pow(10.0, 1.0 / invPowOf10)).
        limit(BigDecimal.valueOf(StrictMath.log10(end / start) * invPowOf10 + 1).
            round(MathContext.UNLIMITED).intValue()).sequential();
  }
}
