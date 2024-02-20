package com.ak.comm.converter.app;

import com.ak.comm.converter.Variable;
import com.ak.numbers.Coefficients;
import com.ak.numbers.Interpolators;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.function.IntUnaryOperator;

public abstract class AbstractSplineCoefficientsChartApp<X extends Enum<X> & Variable<X>, Y extends Enum<Y> & Variable<Y>>
    extends Application {
  private final Coefficients coefficients;
  private final X xVariable;
  private final Y yVariable;

  protected AbstractSplineCoefficientsChartApp(Coefficients coefficients, X xVariable, Y yVariable) {
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
    xAxis.setLabel(xVariable.name());
    xAxis.setAutoRanging(true);

    Axis<Number> yAxis = new NumberAxis();
    yAxis.setLabel(yVariable.name());
    yAxis.setAutoRanging(true);

    ObservableList<XYChart.Data<Number, Number>> pureData = FXCollections.observableArrayList();
    for (double[] aXAndY : xAndY) {
      pureData.add(new XYChart.Data<>(aXAndY[0], aXAndY[1]));
    }

    ObservableList<XYChart.Data<Number, Number>> splineData = FXCollections.observableArrayList();
    IntUnaryOperator f = Interpolators.interpolator(coefficients).get();
    for (var i = 0; i < xAndY[xAndY.length - 1][0]; i++) {
      splineData.add(new XYChart.Data<>(i, f.applyAsInt(i)));
    }

    ObservableList<XYChart.Series<Number, Number>> lineChartData = FXCollections.observableArrayList();
    lineChartData.add(new LineChart.Series<>("Spline", splineData));
    lineChartData.add(new LineChart.Series<>(coefficients.toString(), pureData));

    LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis, lineChartData);
    chart.setCreateSymbols(true);
    chart.setLegendSide(Side.TOP);
    return chart;
  }
}