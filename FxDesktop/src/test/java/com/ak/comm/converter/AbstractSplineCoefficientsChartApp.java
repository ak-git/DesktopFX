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

public abstract class AbstractSplineCoefficientsChartApp<X extends Enum<X> & Variable<X>, Y extends Enum<Y> & Variable<Y>>
    extends Application {
  @Nonnull
  private final Coefficients coefficients;
  @Nonnull
  private final Variable<X> xVariable;
  @Nonnull
  private final Variable<Y> yVariable;

  protected AbstractSplineCoefficientsChartApp(@Nonnull Coefficients coefficients, @Nonnull Variable<X> xVariable, @Nonnull Variable<Y> yVariable) {
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
}