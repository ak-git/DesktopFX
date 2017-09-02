package com.ak.fx.scene;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Nonnull;
import javax.measure.quantity.Speed;

import com.ak.comm.converter.Variable;
import com.ak.comm.converter.Variables;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.input.ScrollEvent;
import javafx.scene.text.Text;
import tec.uom.se.quantity.Quantities;
import tec.uom.se.unit.MetricPrefix;
import tec.uom.se.unit.Units;

import static com.ak.fx.scene.GridCell.BIG;
import static com.ak.fx.scene.GridCell.POINTS;
import static com.ak.fx.scene.GridCell.SMALL;

public final class Chart<EV extends Enum<EV> & Variable<EV>> extends AbstractRegion {
  private static final double[] EMPTY_DOUBLES = {};
  private final MilliGrid milliGrid = new MilliGrid();
  private final List<LineDiagram> lineDiagrams = new ArrayList<>();
  private final Text xAxisUnit = new Text(Variables.toString(
      Quantities.getQuantity(25, MetricPrefix.MILLI(Units.METRE).divide(Units.SECOND).asType(Speed.class)))
  );
  private final IntegerProperty startProperty = new SimpleIntegerProperty();
  private final IntegerProperty lengthProperty = new SimpleIntegerProperty();

  public Chart() {
    getChildren().add(milliGrid);
    xAxisUnit.setFont(Constants.FONT);
  }

  public void setVariables(Collection<EV> variables) {
    lineDiagrams.addAll(variables.stream().map(ev -> new LineDiagram(Variables.toString(ev))).collect(Collectors.toList()));
    lineDiagrams.forEach(lineDiagram -> lineDiagram.setXStep(1.0));
    getChildren().addAll(lineDiagrams);
    getChildren().add(xAxisUnit);

    setOnScroll((ScrollEvent event) -> {
      startProperty.setValue((int) Math.max(0, Math.rint(startProperty.get() - event.getDeltaX())));
      event.consume();
    });
  }

  public void setAll(@Nonnull List<int[]> chartData) {
    if (chartData.isEmpty()) {
      lineDiagrams.forEach(lineDiagram -> lineDiagram.setAll(EMPTY_DOUBLES));
      startProperty.setValue(0);
    }
    else {
      for (int i = 0; i < chartData.size(); i++) {
        int[] ints = chartData.get(i);
        lineDiagrams.get(i).setAll(IntStream.of(ints).mapToDouble(value -> value).toArray());
      }
      int realDataLen = chartData.get(0).length;
      if (realDataLen < lengthProperty.get()) {
        startProperty.setValue(Math.max(0, startProperty.get() + realDataLen - lengthProperty.get()));
      }
    }
  }

  public ReadOnlyIntegerProperty startProperty() {
    return ReadOnlyIntegerProperty.readOnlyIntegerProperty(startProperty);
  }

  public ReadOnlyIntegerProperty lengthProperty() {
    return ReadOnlyIntegerProperty.readOnlyIntegerProperty(lengthProperty);
  }

  @Override
  void layoutChartChildren(double x, double y, double width, double height) {
    milliGrid.resizeRelocate(x, y, width, height);
    layoutLineDiagrams(x + SMALL.minCoordinate(width), y + SMALL.minCoordinate(height), SMALL.maxWidth(width), height);
    layoutText(x + SMALL.minCoordinate(width), y + SMALL.minCoordinate(height), SMALL.maxWidth(width));
    int prevChartCenter = startProperty.get() + lengthProperty.get() / 2;
    lengthProperty.setValue(lineDiagrams.get(0).getMaxSamples());
    startProperty.setValue(Math.max(0, prevChartCenter - lengthProperty.get() / 2));
  }

  private void layoutText(double x, double y, double width) {
    xAxisUnit.relocate(x + BIG.minCoordinate(width) + BIG.maxWidth(width) / 2 + POINTS.getStep(),
        y + SMALL.getStep() / 2 - Constants.LABEL_HEIGHT);
  }

  private void layoutLineDiagrams(double x, double y, double width, double height) {
    double dHeight = SMALL.maxWidth(height * 2 / (1 + lineDiagrams.size()));

    for (LineDiagram rectangle : lineDiagrams) {
      rectangle.resizeRelocate(x, y, width, dHeight);
    }

    if (dHeight >= SMALL.getStep() * 2) {
      for (int i = 0; i < lineDiagrams.size() / 2; i++) {
        lineDiagrams.get(i).relocate(x, y + SMALL.roundCoordinate(height / (lineDiagrams.size() + 1)) * i);
      }
      if ((lineDiagrams.size() & 1) != 0) {
        lineDiagrams.get(lineDiagrams.size() / 2).relocate(x, y + SMALL.maxWidth(height) / 2 - dHeight / 2);
      }
      for (int i = 0; i < lineDiagrams.size() / 2; i++) {
        lineDiagrams.get(lineDiagrams.size() - 1 - i).
            relocate(x, y + SMALL.maxWidth(height) -
                dHeight - SMALL.roundCoordinate(height / (lineDiagrams.size() + 1)) * i);
      }
    }
    else {
      lineDiagrams.forEach(rectangle -> rectangle.resize(width, SMALL.maxWidth(height)));
    }
  }
}
