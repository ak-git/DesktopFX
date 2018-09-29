package com.ak.comm.converter;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.IntSummaryStatistics;
import java.util.function.Function;
import java.util.function.IntBinaryOperator;
import java.util.function.IntUnaryOperator;
import java.util.function.Supplier;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import javax.annotation.Nonnull;

import com.ak.numbers.Coefficients;
import com.ak.numbers.CoefficientsUtils;
import com.ak.numbers.Interpolators;
import com.ak.numbers.aper.sincos.AperSurfaceCoefficientsChannel2;
import com.ak.util.LineFileBuilder;
import com.ak.util.LineFileCollector;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ValueAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.testng.Assert;

public abstract class AbstractSplineCoefficientsChartApp<X extends Enum<X> & Variable<X>, Y extends Enum<Y> & Variable<Y>>
    extends Application {
  @Nonnull
  private final Coefficients coefficients;
  @Nonnull
  private final X xVariable;
  @Nonnull
  private final Y yVariable;

  protected AbstractSplineCoefficientsChartApp(@Nonnull Coefficients coefficients, @Nonnull X xVariable, @Nonnull Y yVariable) {
    this.coefficients = coefficients;
    this.xVariable = xVariable;
    this.yVariable = yVariable;
  }

  @Override
  public void start(Stage primaryStage) {
    primaryStage.setScene(new Scene(createContent()));
    primaryStage.setWidth(Screen.getPrimary().getVisualBounds().getWidth());
    primaryStage.setHeight(Screen.getPrimary().getVisualBounds().getHeight());
    primaryStage.centerOnScreen();
    primaryStage.show();
  }

  private Parent createContent() {
    double[][] xAndY = coefficients.getPairs();
    ValueAxis<Number> xAxis = new NumberAxis();
    xAxis.setLabel(Variables.toName(xVariable));
    xAxis.setAutoRanging(true);

    Axis<Number> yAxis = new NumberAxis();
    yAxis.setLabel(Variables.toName(yVariable));
    yAxis.setAutoRanging(true);

    ObservableList<XYChart.Data<Number, Number>> pureData = FXCollections.observableArrayList();
    for (double[] aXAndY : xAndY) {
      pureData.add(new XYChart.Data<>(aXAndY[0], aXAndY[1]));
    }

    ObservableList<XYChart.Data<Number, Number>> splineData = FXCollections.observableArrayList();
    IntUnaryOperator f = Interpolators.interpolator(coefficients).get();
    for (int i = 0; i < xAndY[xAndY.length - 1][0]; i++) {
      splineData.add(new XYChart.Data<>(i, f.applyAsInt(i)));
    }

    ObservableList<XYChart.Series<Number, Number>> lineChartData = FXCollections.observableArrayList();
    lineChartData.add(new LineChart.Series<>("Spline", splineData));
    lineChartData.add(new LineChart.Series<>(coefficients.name(), pureData));

    LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis, lineChartData);
    chart.setCreateSymbols(true);
    chart.setLegendSide(Side.TOP);
    return chart;
  }

  public static <C extends Enum<C> & Coefficients> void testSplineSurface1(Class<C> surfaceCoeffClass) throws IOException {
    IntBinaryOperator function = Interpolators.interpolator(surfaceCoeffClass).get();
    LineFileBuilder.of("%.0f %.0f %.0f").
        xStream(() -> intRange(surfaceCoeffClass, CoefficientsUtils::rangeX).asDoubleStream()).
        yStream(() -> intRange(surfaceCoeffClass, CoefficientsUtils::rangeY).asDoubleStream()).
        generate("z.txt", (adc, rII) -> function.applyAsInt(Double.valueOf(adc).intValue(), Double.valueOf(rII).intValue()));

    Supplier<DoubleStream> xVar = () -> intRange(surfaceCoeffClass, CoefficientsUtils::rangeX).asDoubleStream();
    Assert.assertTrue(xVar.get().mapToObj(sToL -> String.format("%.2f", sToL)).collect(
        new LineFileCollector(Paths.get("x.txt"), LineFileCollector.Direction.HORIZONTAL)));

    Supplier<DoubleStream> yVar = () -> intRange(surfaceCoeffClass, CoefficientsUtils::rangeY).asDoubleStream();
    Assert.assertTrue(yVar.get().mapToObj(sToL -> String.format("%.2f", sToL)).collect(
        new LineFileCollector(Paths.get("y.txt"), LineFileCollector.Direction.VERTICAL)));
  }

  public static <C extends Enum<C> & Coefficients> void testSplineSurface2(Class<C> surfaceCoeffClass) throws IOException {
    IntBinaryOperator function = Interpolators.interpolator(surfaceCoeffClass).get();
    LineFileBuilder.of("%.0f %.0f %.0f").
        xStream(() -> intRange(AperSurfaceCoefficientsChannel2.class, CoefficientsUtils::rangeX).asDoubleStream()).
        yStream(() -> intRange(AperSurfaceCoefficientsChannel2.class, CoefficientsUtils::rangeY).asDoubleStream()).
        generate("z.txt", (adc, rII) -> function.applyAsInt(Double.valueOf(adc).intValue(), Double.valueOf(rII).intValue()));
  }

  private static <C extends Enum<C> & Coefficients> IntStream intRange(@Nonnull Class<C> coeffClass,
                                                                       @Nonnull Function<Class<C>, IntSummaryStatistics> selector) {
    int countValues = 100;
    IntSummaryStatistics statistics = selector.apply(coeffClass);
    int step = Math.max(1, (statistics.getMax() - statistics.getMin()) / countValues);
    return IntStream.rangeClosed(0, countValues).map(i -> statistics.getMin() + i * step);
  }
}