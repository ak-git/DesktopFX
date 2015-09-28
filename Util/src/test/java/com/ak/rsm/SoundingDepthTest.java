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

  private static PointValuePair solve(DoubleUnaryOperator inequality) {
    SimplexOptimizer optimizer = new SimplexOptimizer(-1, 1.0e-6);
    return optimizer.optimize(new MaxEval(100), new ObjectiveFunction(
            hToL -> inequality.applyAsDouble(hToL[0])), GoalType.MINIMIZE,
        new NelderMeadSimplex(1, 0.01),
        new InitialGuess(new double[] {0.5})
    );
  }

  @DataProvider(name = "sToL-k12")
  public static Object[][] sToLbyK12() {
    Supplier<DoubleStream> xVar = () -> doubleRange(0.01, 0.99, 1.0e-2);
    xVar.get().mapToObj(value -> String.format("%.2f", value)).collect(
        new LineFileCollector<>(Paths.get("x.txt"), LineFileCollector.Direction.HORIZONTAL));

    Supplier<DoubleStream> yVar = () -> doubleRange(-0.99, 0.99, 1.0e-2);
    yVar.get().mapToObj(value -> String.format("%.2f", value)).collect(
        new LineFileCollector<>(Paths.get("y.txt"), LineFileCollector.Direction.VERTICAL));
    return new Object[][] {{xVar, yVar}};
  }

  @Test(dataProvider = "sToL-k12", enabled = false)
  public void testRho1SameRho2byK12(Supplier<DoubleStream> xVar, Supplier<DoubleStream> yVar) {
    yVar.get().mapToObj(k12 -> xVar.get().map(sToL -> solve(new InequalityRbyRho2(k12, sToL)).getKey()[0])).
        map(stream -> stream.mapToObj(value -> String.format("%.6f", value)).collect(Collectors.joining("\t"))).
        collect(new LineFileCollector<>(Paths.get("z.txt"), LineFileCollector.Direction.VERTICAL));
  }

  @DataProvider(name = "sToL-rho")
  public static Object[][] sToLbyRho1Rho2() {
    Supplier<DoubleStream> xVar = () -> doubleRange(0.01, 0.99, 1.0e-2);
    xVar.get().mapToObj(value -> String.format("%.2f", value)).collect(
        new LineFileCollector<>(Paths.get("x.txt"), LineFileCollector.Direction.HORIZONTAL));

    Supplier<DoubleStream> yVar = () -> log10DoubleRange(1.0e-2, 1.0e2, 16.0);
    yVar.get().mapToObj(value -> String.format("%.4f", value)).collect(
        new LineFileCollector<>(Paths.get("y.txt"), LineFileCollector.Direction.VERTICAL));
    return new Object[][] {{xVar, yVar}};
  }

  @Test(dataProvider = "sToL-rho", enabled = false)
  public void testRho1SameRho2byRho(Supplier<DoubleStream> xVar, Supplier<DoubleStream> yVar) {
    yVar.get().mapToObj(rhoToRho -> xVar.get().map(sToL -> solve(
        new InequalityRbyRho2(ResistanceTwoLayer.getK12(rhoToRho, 1.0), sToL)).getKey()[0])).
        map(stream -> stream.mapToObj(value -> String.format("%.6f", value)).collect(Collectors.joining("\t"))).
        collect(new LineFileCollector<>(Paths.get("z.txt"), LineFileCollector.Direction.VERTICAL));
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
