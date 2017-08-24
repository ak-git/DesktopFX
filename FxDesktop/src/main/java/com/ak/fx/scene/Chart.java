package com.ak.fx.scene;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import javax.measure.quantity.Speed;

import com.ak.comm.converter.Variable;
import com.ak.comm.converter.Variables;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.scene.text.Text;
import javafx.util.Duration;
import tec.uom.se.quantity.Quantities;
import tec.uom.se.unit.MetricPrefix;
import tec.uom.se.unit.Units;

import static com.ak.fx.scene.GridCell.POINTS;
import static com.ak.fx.scene.GridCell.SMALL;

public final class Chart<EV extends Enum<EV> & Variable<EV>> extends AbstractRegion {
  private final MilliGrid milliGrid = new MilliGrid();
  private final List<LineDiagram> lineDiagrams = new ArrayList<>();
  private final Text xAxisUnit = new Text(Variables.toString(
      Quantities.getQuantity(25, MetricPrefix.MILLI(Units.METRE).divide(Units.SECOND).asType(Speed.class)))
  );

  public Chart() {
    getChildren().add(milliGrid);

    xAxisUnit.setFont(Constants.FONT);

    Timeline timeLine = new Timeline();
    timeLine.getKeyFrames().add(
        new KeyFrame(Duration.millis(250), (ActionEvent actionEvent) -> lineDiagrams.forEach(lineDiagram -> {
          Random random = new Random();

          double xStep = 1.0;
          lineDiagram.setAll(xStep, DoubleStream.generate(() -> random.nextGaussian() * 30).limit(
              lineDiagram.getMaxSamples(xStep)).toArray());
        }))
    );
    timeLine.setCycleCount(Animation.INDEFINITE);
    SequentialTransition animation = new SequentialTransition();
    animation.getChildren().addAll(timeLine);
    animation.play();
  }

  public void setVariables(Collection<EV> variables) {
    lineDiagrams.addAll(variables.stream().map(ev -> new LineDiagram(Variables.toString(ev))).collect(Collectors.toList()));
    getChildren().addAll(lineDiagrams);
    getChildren().add(xAxisUnit);
  }

  @Override
  void layoutChartChildren(double x, double y, double width, double height) {
    milliGrid.resizeRelocate(x, y, width, height);
    layoutLineDiagrams(x + SMALL.minCoordinate(width), y + SMALL.minCoordinate(height),
        SMALL.maxCoordinate(width), height);
    layoutText(x + SMALL.minCoordinate(width), y + SMALL.minCoordinate(height),
        SMALL.maxCoordinate(width));
  }

  private void layoutText(double x, double y, double width) {
    xAxisUnit.relocate(x + SMALL.maxCoordinate(width) - SMALL.getStep() * 2 + POINTS.getStep(),
        y + SMALL.getStep() / 2 - Constants.LABEL_HEIGHT);

  }

  private void layoutLineDiagrams(double x, double y, double width, double height) {
    double dHeight = SMALL.maxCoordinate(height * 2 / (1 + lineDiagrams.size()));

    for (LineDiagram rectangle : lineDiagrams) {
      rectangle.resizeRelocate(x, y, width, dHeight);
    }

    if (dHeight >= SMALL.getStep() * 2) {
      for (int i = 0; i < lineDiagrams.size() / 2; i++) {
        lineDiagrams.get(i).relocate(x, y + SMALL.roundCoordinate(height / (lineDiagrams.size() + 1)) * i);
      }
      if ((lineDiagrams.size() & 1) != 0) {
        lineDiagrams.get(lineDiagrams.size() / 2).relocate(x, y + SMALL.maxCoordinate(height) / 2 - dHeight / 2);
      }
      for (int i = 0; i < lineDiagrams.size() / 2; i++) {
        lineDiagrams.get(lineDiagrams.size() - 1 - i).
            relocate(x, y + SMALL.maxCoordinate(height) -
                dHeight - SMALL.roundCoordinate(height / (lineDiagrams.size() + 1)) * i);
      }
    }
    else {
      lineDiagrams.forEach(rectangle -> rectangle.resize(width, SMALL.maxCoordinate(height)));
    }
  }
}
