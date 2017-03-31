package com.ak.numbers.aper;

import java.util.function.IntUnaryOperator;

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
import tec.uom.se.unit.Units;


public final class AperChartApp extends Application {
  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    primaryStage.setScene(new Scene(createContent()));
    primaryStage.setWidth(Screen.getPrimary().getVisualBounds().getWidth());
    primaryStage.setHeight(Screen.getPrimary().getVisualBounds().getHeight());
    primaryStage.centerOnScreen();
    primaryStage.show();
  }

  private static Parent createContent() {
    AperCoefficients coefficients = AperCoefficients.I_ADC_TO_OHM;

    double[] xAndY = coefficients.get();
    ValueAxis<Number> xAxis = new NumberAxis(xAndY[0], xAndY[xAndY.length - 2], 100);
    xAxis.setLabel("Samples ADC");
    xAxis.setAutoRanging(true);

    Axis<Number> yAxis = new NumberAxis();
    yAxis.setLabel("R(I-I), " + Units.OHM);
    yAxis.setAutoRanging(true);

    ObservableList<XYChart.Data<Number, Number>> pureData = FXCollections.observableArrayList();
    for (int i = 0; i < xAndY.length / 2; i++) {
      pureData.add(new XYChart.Data<>(xAndY[i * 2], xAndY[i * 2 + 1]));
    }

    ObservableList<XYChart.Data<Number, Number>> splineData = FXCollections.observableArrayList();
    IntUnaryOperator f = Interpolators.interpolate(coefficients).get();
    for (int i = 0; i < xAxis.getUpperBound(); i++) {
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