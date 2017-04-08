package com.ak.comm.converter;

import java.util.function.IntUnaryOperator;

import javax.annotation.Nonnull;

import com.ak.numbers.Coefficients;
import com.ak.numbers.Interpolators;
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

import static java.lang.StrictMath.log;
import static java.lang.StrictMath.pow;

public abstract class AbstractSplineCoefficientsChartApp
    extends Application {
  @Nonnull
  private final Coefficients coefficients;
  @Nonnull
  private final Variable xVariable;
  @Nonnull
  private final Variable yVariable;

  public AbstractSplineCoefficientsChartApp(@Nonnull Coefficients coefficients, @Nonnull Variable xVariable, @Nonnull Variable yVariable) {
    this.coefficients = coefficients;
    this.xVariable = xVariable;
    this.yVariable = yVariable;
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    primaryStage.setScene(new Scene(createContent()));
    primaryStage.setWidth(Screen.getPrimary().getVisualBounds().getWidth());
    primaryStage.setHeight(Screen.getPrimary().getVisualBounds().getHeight());
    primaryStage.centerOnScreen();
    primaryStage.show();
  }

  private Parent createContent() {
    double[][] xAndY = coefficients.getPairs();
    ValueAxis<Number> xAxis = new NumberAxis(xAndY[0][0], xAndY[xAndY.length - 1][0],
        getOptimalInterval(xAndY[xAndY.length - 1][0] - xAndY[0][0]));
    xAxis.setLabel(xVariable.toName());
    xAxis.setAutoRanging(true);

    Axis<Number> yAxis = new NumberAxis();
    yAxis.setLabel(yVariable.toName());
    yAxis.setAutoRanging(true);

    ObservableList<XYChart.Data<Number, Number>> pureData = FXCollections.observableArrayList();
    for (double[] aXAndY : xAndY) {
      pureData.add(new XYChart.Data<>(aXAndY[0], aXAndY[1]));
    }

    ObservableList<XYChart.Data<Number, Number>> splineData = FXCollections.observableArrayList();
    IntUnaryOperator f = Interpolators.interpolator(coefficients).get();
    for (int i = 0; i < xAxis.getUpperBound() + 1; i++) {
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

  private static double getOptimalInterval(double a) {
    double lgx = log(a) / log(10.0);
    double x = Math.floor(lgx);
    double dx0 = log(1.1) / log(10.0);
    double dx1 = log(4.1) / log(10.0);
    double dx2 = log(7.1) / log(10.0);
    if (lgx >= x && (lgx < x + dx0)) {
      return pow(10.0, x - 1.0) * 5.0;
    }
    else if ((lgx >= x + dx0) && (lgx < x + dx1)) {
      return pow(10.0, x);
    }
    else if ((lgx >= x + dx1) && (lgx < x + dx2)) {
      return pow(10.0, x) * 3.0;
    }
    else {
      return pow(10.0, x) * 5.0;
    }
  }
}