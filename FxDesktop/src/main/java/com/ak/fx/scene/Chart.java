package com.ak.fx.scene;

import java.util.ArrayList;
import java.util.Collection;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.measure.quantity.Speed;

import com.ak.comm.converter.Variable;
import com.ak.comm.converter.Variables;
import com.ak.digitalfilter.Filters;
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
  private final List<EV> variables = new ArrayList<>();
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
    xAxisUnit.setFont(Constants.FONT_H1);
  }

  public void setVariables(@Nonnull Collection<EV> variables) {
    this.variables.addAll(variables);
    lineDiagrams.addAll(variables.stream().map(ev -> new LineDiagram(Variables.toString(ev))).collect(Collectors.toList()));
    getChildren().addAll(lineDiagrams);
    getChildren().add(xAxisUnit);

    xAxisUnit.textProperty().bind(zoomXProperty.asString());
    setOnScroll(event -> {
      startProperty.setValue((int) Math.max(0, Math.rint(startProperty.get() - event.getDeltaX() * decimateFactor)));
      event.consume();
    });
    setOnScrollFinished(event -> {
      Logger.getLogger(getClass().getName()).log(Level.CONFIG,
          String.format("Total chart size = %d [%d - %d]; x-zoom = %d mm/s; decimate factor = %d",
              lengthProperty.get(), startProperty.get(), startProperty.get() + lengthProperty.get(),
              zoomXProperty.get().mmPerSec, decimateFactor));
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
    Constants.invokeInFx(() -> {
      if (chartData.isEmpty()) {
        lineDiagrams.forEach(lineDiagram -> lineDiagram.setAll(EMPTY_DOUBLES));
        startProperty.setValue(0);
      }
      else {
        for (int i = 0; i < chartData.size(); i++) {
          double mm = SMALL.getStep() / 10.0;
          double range = lineDiagrams.get(i).getHeight() / mm;
          int[] values = Filters.smoothingDecimate(chartData.get(i), decimateFactor);
          IntSummaryStatistics intSummaryStatistics = IntStream.of(values).summaryStatistics();
          if (intSummaryStatistics.getMax() == intSummaryStatistics.getMin()) {
            intSummaryStatistics = IntStream.of(intSummaryStatistics.getMax(), 0).summaryStatistics();
          }

          int meanScaleFactor10 = scaleFactor10(range, intSummaryStatistics.getMax() - intSummaryStatistics.getMin()) * 10;
          int mean = (int) Math.rint((intSummaryStatistics.getMax() + intSummaryStatistics.getMin()) / 2.0 / meanScaleFactor10) * meanScaleFactor10;
          int signalRange = Math.max(Math.abs(intSummaryStatistics.getMax() - mean), Math.abs(intSummaryStatistics.getMin() - mean)) * 2;
          int scaleFactor10 = scaleFactor10(range, signalRange);
          int scaleFactor = optimizeScaleY(range, signalRange);

          int finalI = i;
          lineDiagrams.get(i).setYLabelsGenerator(mmIndex -> {
                double yCoordinate = lineDiagrams.get(finalI).getCenter() - mmIndex * mm;
                boolean visible = true;

                if (finalI > 0) {
                  visible = Math.abs(yCoordinate - lineDiagrams.get(finalI).getCenter()) - POINTS.getStep() <
                      Math.abs(yCoordinate - lineDiagrams.get(finalI - 1).getCenter());
                }

                if (finalI < lineDiagrams.size() - 1) {
                  visible &= Math.abs(yCoordinate - lineDiagrams.get(finalI).getCenter()) + POINTS.getStep() <
                      Math.abs(yCoordinate - lineDiagrams.get(finalI + 1).getCenter());
                }

                return visible ? Variables.toString(mean + mmIndex * scaleFactor, variables.get(finalI).getUnit(), scaleFactor10) : null;
              }
          );

          lineDiagrams.get(i).setAll(IntStream.of(values).mapToDouble(value -> mm * (value - mean) / scaleFactor).toArray());
        }
        int realDataLen = chartData.get(0).length;
        if (realDataLen < lengthProperty.get()) {
          startProperty.setValue(Math.max(0, startProperty.get() + realDataLen - lengthProperty.get()));
        }
      }
    });
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
        y + SMALL.getStep() / 2 - xAxisUnit.getFont().getSize());
  }

  private void layoutLineDiagrams(double x, double y, double width, double height) {
    double dHeight = SMALL.maxWidth(height * 2 / (1 + lineDiagrams.size()));
    if (dHeight >= SMALL.getStep() * 2) {
      lineDiagrams.forEach(lineDiagram -> lineDiagram.resizeRelocate(x, y, width, dHeight));
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
      lineDiagrams.forEach(lineDiagram -> lineDiagram.resizeRelocate(x, y, width, SMALL.maxWidth(height)));
    }
  }

  private void setXStep(@Nonnull ZoomX zoomX, @Nonnegative double frequency) {
    double pointsInSec = SMALL.getStep() * zoomX.mmPerSec / 10.0;
    int decimateFactor = (int) Math.rint(frequency / pointsInSec);
    if (decimateFactor > 2) {
      this.decimateFactor = decimateFactor;
    }
    else {
      this.decimateFactor = 1;
    }
    double xStep = this.decimateFactor * pointsInSec / frequency;
    Logger.getLogger(getClass().getName()).log(Level.CONFIG,
        String.format("Frequency = %.0f Hz; x-zoom = %d mm/s; pixels per sec = %.1f; decimate factor = %d; x-step = %.1f px",
            frequency, zoomX.mmPerSec, pointsInSec, this.decimateFactor, xStep));
    lineDiagrams.forEach(lineDiagram -> lineDiagram.setXStep(xStep));
  }

  private static int scaleFactor10(@Nonnegative double range, @Nonnegative int signalRange) {
    return (int) StrictMath.pow(10.0, Math.ceil(Math.max(0, StrictMath.log10(signalRange / range))));
  }

  private static int optimizeScaleY(@Nonnegative double range, @Nonnegative int signalRange) {
    int scaleFactor10 = scaleFactor10(range, signalRange);
    int scaleFactor = scaleFactor10;
    if (range / (signalRange / scaleFactor10) > 5) {
      scaleFactor = scaleFactor10 / 5;
    }
    else if (range / (signalRange / scaleFactor10) > 2) {
      scaleFactor = scaleFactor10 / 2;
    }
    return Math.max(1, scaleFactor);
  }
}
