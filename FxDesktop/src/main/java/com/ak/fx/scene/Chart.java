package com.ak.fx.scene;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.measure.quantity.Speed;

import com.ak.comm.converter.Variable;
import com.ak.comm.converter.Variables;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.text.Text;
import tec.uom.se.quantity.Quantities;
import tec.uom.se.unit.MetricPrefix;
import tec.uom.se.unit.Units;

import static com.ak.fx.scene.GridCell.BIG;
import static com.ak.fx.scene.GridCell.POINTS;
import static com.ak.fx.scene.GridCell.SMALL;

public final class Chart<EV extends Enum<EV> & Variable<EV>> extends AbstractRegion {
  public enum ZoomX {
    Z_10(10), Z_25(25), Z_50(50);

    private final int mmPerSec;

    ZoomX(int mmPerSec) {
      this.mmPerSec = mmPerSec;
    }

    ZoomX prev() {
      return values()[Math.max(0, ordinal() - 1)];
    }

    ZoomX next() {
      return values()[Math.min(values().length - 1, ordinal() + 1)];
    }

    @Override
    public String toString() {
      return Variables.toString(
          Quantities.getQuantity(mmPerSec, MetricPrefix.MILLI(Units.METRE).divide(Units.SECOND).asType(Speed.class))
      );
    }
  }

  private static final double[] EMPTY_DOUBLES = {};
  private final MilliGrid milliGrid = new MilliGrid();
  private final List<LineDiagram> lineDiagrams = new ArrayList<>();
  private final Text xAxisUnit = new Text();
  private final IntegerProperty startProperty = new SimpleIntegerProperty();
  private final IntegerProperty lengthProperty = new SimpleIntegerProperty();
  private final ObjectProperty<ZoomX> zoomXProperty = new SimpleObjectProperty<>(ZoomX.Z_25);
  @Nonnegative
  private int decimateFactor = 1;

  public Chart() {
    getChildren().add(milliGrid);
    xAxisUnit.setFont(Constants.FONT);
  }

  public void setVariables(@Nonnull Collection<EV> variables) {
    lineDiagrams.addAll(variables.stream().map(ev -> new LineDiagram(Variables.toString(ev))).collect(Collectors.toList()));
    getChildren().addAll(lineDiagrams);
    getChildren().add(xAxisUnit);

    xAxisUnit.textProperty().bind(zoomXProperty.asString());
    setOnScroll(event -> {
      startProperty.setValue((int) Math.max(0, Math.rint(startProperty.get() - event.getDeltaX() * decimateFactor)));
      event.consume();
    });
    setOnZoomStarted(event -> {
      if (event.getZoomFactor() > 1) {
        zoomXProperty.setValue(zoomXProperty.get().next());
      }
      else if (event.getZoomFactor() < 1) {
        zoomXProperty.setValue(zoomXProperty.get().prev());
      }
      event.consume();
    });
  }

  public void setFrequency(@Nonnegative double frequency) {
    setXStep(zoomXProperty.get(), frequency);
    zoomXProperty.addListener((observable, oldValue, newValue) -> setXStep(newValue, frequency));
  }

  public void setAll(@Nonnull List<int[]> chartData) {
    if (chartData.isEmpty()) {
      lineDiagrams.forEach(lineDiagram -> lineDiagram.setAll(EMPTY_DOUBLES));
      startProperty.setValue(0);
    }
    else {
      for (int i = 0; i < chartData.size(); i++) {
        int[] ints = chartData.get(i);
        int[] decimated = new int[ints.length / decimateFactor];
        for (int j = 0; j < decimated.length; j++) {
          decimated[j] = ints[j * decimateFactor];
        }
        lineDiagrams.get(i).setAll(IntStream.of(decimated).mapToDouble(value -> value).toArray());
      }
      int realDataLen = chartData.get(0).length;
      if (realDataLen < lengthProperty.get()) {
        startProperty.setValue(Math.max(0, startProperty.get() + realDataLen - lengthProperty.get()));
      }
    }
  }

  public ReadOnlyIntegerProperty startProperty() {
    return startProperty;
  }

  public ReadOnlyIntegerProperty lengthProperty() {
    return lengthProperty;
  }

  public ReadOnlyObjectProperty<ZoomX> zoomXProperty() {
    return zoomXProperty;
  }

  @Override
  void layoutChartChildren(double x, double y, double width, double height) {
    milliGrid.resizeRelocate(x, y, width, height);
    layoutLineDiagrams(x + SMALL.minCoordinate(width), y + SMALL.minCoordinate(height), SMALL.maxWidth(width), height);
    layoutText(x + SMALL.minCoordinate(width), y + SMALL.minCoordinate(height), SMALL.maxWidth(width));
    int prevChartCenter = startProperty.get() + lengthProperty.get() / 2;
    lengthProperty.setValue(lineDiagrams.get(0).getMaxSamples() * decimateFactor);
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

  private void setXStep(@Nonnull ZoomX zoomX, @Nonnegative double frequency) {
    double pointsInSec = SMALL.getStep() * zoomX.mmPerSec / 10;
    decimateFactor = (int) Math.rint(frequency / pointsInSec);
    lineDiagrams.forEach(lineDiagram -> lineDiagram.setXStep(frequency / decimateFactor / pointsInSec));
  }
}
