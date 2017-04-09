package com.ak.fx.desktop.aper;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import com.ak.comm.GroupService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public final class AperViewController {
  private static final int INT = 4000;
  @Nonnull
  private final List<LineChart<Number, Number>> lineCharts = new LinkedList<>();
  @FXML
  private VBox root;
  private int index = -1;

  @Inject
  public AperViewController(@Nonnull GroupService<?, ?, ?> service) {
    service.subscribe(ints -> Platform.runLater(() -> {
      if (lineCharts.isEmpty()) {
        lineCharts.addAll(Arrays.stream(ints).mapToObj(value -> createChart()).collect(Collectors.toList()));
        lineCharts.forEach(lineChart -> root.getChildren().add(new BorderPane(lineChart)));
      }

      index = (++index) % INT;
      for (int i = 0; i < ints.length; i++) {
        lineCharts.get(i).getData().get(0).getData().set(index, new XYChart.Data<>(index, ints[i]));
      }
    }));
  }

  private static LineChart<Number, Number> createChart() {
    NumberAxis xAxis = new NumberAxis(0, INT, 200);
    xAxis.setAutoRanging(false);

    NumberAxis yAxis = new NumberAxis();
    yAxis.setForceZeroInRange(false);

    LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
    lineChart.setLegendVisible(false);
    lineChart.setCreateSymbols(false);
    lineChart.setAnimated(false);

    lineChart.getData().add(new XYChart.Series<>());
    for (int i = 0; i < INT; i++) {
      lineChart.getData().get(0).getData().add(new XYChart.Data<>(i, 0));
    }
    return lineChart;
  }
}