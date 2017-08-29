package com.ak.fx.scene;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.measure.quantity.Speed;

import com.ak.comm.converter.Variable;
import com.ak.comm.converter.Variables;
import javafx.scene.input.ScrollEvent;
import javafx.scene.text.Text;
import tec.uom.se.quantity.Quantities;
import tec.uom.se.unit.MetricPrefix;
import tec.uom.se.unit.Units;

import static com.ak.fx.scene.GridCell.BIG;
import static com.ak.fx.scene.GridCell.POINTS;
import static com.ak.fx.scene.GridCell.SMALL;

public final class Chart<EV extends Enum<EV> & Variable<EV>> extends AbstractRegion {
  private final MilliGrid milliGrid = new MilliGrid();
  private final List<LineDiagram> lineDiagrams = new ArrayList<>();
  private final Text xAxisUnit = new Text(Variables.toString(
      Quantities.getQuantity(25, MetricPrefix.MILLI(Units.METRE).divide(Units.SECOND).asType(Speed.class)))
  );
  private int start;

  public Chart() {
    getChildren().add(milliGrid);
    xAxisUnit.setFont(Constants.FONT);

    setOnScroll((ScrollEvent event) -> {
      start = (int) Math.max(0, Math.rint(start - event.getDeltaX()));

      for (int i = 0; i < lineDiagrams.stream().mapToInt(LineDiagram::getMaxSamples).summaryStatistics().getMin(); i++) {
        int finalI = i;
        lineDiagrams.forEach(lineDiagram -> lineDiagram.add(50 * StrictMath.sin((finalI + start) / 100.0)));
      }

      event.consume();
    });
  }

  public void setVariables(Collection<EV> variables) {
    lineDiagrams.addAll(variables.stream().map(ev -> new LineDiagram(Variables.toString(ev))).collect(Collectors.toList()));
    lineDiagrams.forEach(lineDiagram -> lineDiagram.setXStep(1.0));
    getChildren().addAll(lineDiagrams);
    getChildren().add(xAxisUnit);
  }

  @Override
  void layoutChartChildren(double x, double y, double width, double height) {
    milliGrid.resizeRelocate(x, y, width, height);
    layoutLineDiagrams(x + SMALL.minCoordinate(width), y + SMALL.minCoordinate(height), SMALL.maxCoordinate(width), height);
    layoutText(x + SMALL.minCoordinate(width), y + SMALL.minCoordinate(height), SMALL.maxCoordinate(width));
  }

  private void layoutText(double x, double y, double width) {
    xAxisUnit.relocate(x + BIG.minCoordinate(width) + BIG.maxCoordinate(width) / 2 + POINTS.getStep(),
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
