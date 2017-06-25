package com.ak.fx.desktop.aper;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import com.ak.comm.GroupService;
import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.converter.Variable;
import com.ak.comm.converter.Variables;
import com.ak.fx.desktop.AbstractViewController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

public abstract class AbstractAperViewController<EV extends Enum<EV> & Variable<EV>>
    extends AbstractViewController<BufferFrame, BufferFrame, EV> {
  private static final int INT = 4000;
  @Nonnull
  private final List<LineChart<Number, Number>> lineCharts = new LinkedList<>();
  @FXML
  private Label qOs1Label;
  @FXML
  private Label qOs2Label;
  private int index = -1;

  public AbstractAperViewController(@Nonnull GroupService<BufferFrame, BufferFrame, EV> service) {
    super(service);
    List<EV> variables = service.getVariables();

    service.subscribe(ints -> Platform.runLater(() -> {
      index = (++index) % INT;

      for (int i = 0, list = 0; i < ints.length; i++) {
        EV ev = variables.get(i);
        if (ev.isVisible()) {
          lineCharts.get(list).getData().get(0).getData().set(index, new XYChart.Data<>(index, ints[i]));
          list++;
        }
        else {
          if ("RI1".equals(ev.name())) {
            qOs1Label.setText(Variables.toString(ev, ints[i]));
          }
          else if ("RI2".equals(ev.name())) {
            qOs2Label.setText(Variables.toString(ev, ints[i]));
          }
        }
      }
    }));
  }

  @Override
  public final void initialize(@Nonnull URL location, @Nonnull ResourceBundle resources) {
    super.initialize(location, resources);
    lineCharts.addAll(service().getVariables().stream().filter(ev -> ev.isVisible()).map(v -> createChart()).collect(Collectors.toList()));
    lineCharts.forEach(lineChart -> root().getChildren().add(new BorderPane(lineChart)));
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