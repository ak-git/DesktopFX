package com.ak.fx.scene;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.measure.quantity.Speed;

import com.ak.comm.converter.Variables;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import tec.uom.se.quantity.Quantities;
import tec.uom.se.unit.MetricPrefix;
import tec.uom.se.unit.Units;

import static com.ak.fx.scene.GridCell.SMALL;

public final class AxisXController {
  public enum ZoomX {
    Z_10(10), Z_25(25), Z_50(50);

    private final int mmPerSec;

    ZoomX(@Nonnegative int mmPerSec) {
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

  private final IntegerProperty startProperty = new SimpleIntegerProperty();
  private final IntegerProperty lengthProperty = new SimpleIntegerProperty();
  private final ObjectProperty<ZoomX> zoomProperty = new SimpleObjectProperty<>(ZoomX.Z_25);
  private final DoubleProperty stepProperty = new SimpleDoubleProperty();
  @Nonnegative
  private int decimateFactor = 1;

  public AxisXController(@Nonnull Runnable onUpdate) {
    startProperty.addListener((observable, oldValue, newValue) -> onUpdate.run());
    lengthProperty.addListener((observable, oldValue, newValue) -> onUpdate.run());
  }

  @Override
  public String toString() {
    return String.format("Axis-X size = %d [%d - %d]; x-zoom = %d mm/s; decimate factor = %d",
        lengthProperty.get(), getStart(), getEnd(), zoomProperty.get().mmPerSec, decimateFactor);
  }

  public void setFrequency(@Nonnegative double frequency) {
    setStep(frequency);
    zoomProperty.addListener((observable, oldValue, newValue) -> setStep(frequency));
  }

  public ReadOnlyObjectProperty<ZoomX> zoomProperty() {
    return zoomProperty;
  }

  public ReadOnlyDoubleProperty stepProperty() {
    return stepProperty;
  }

  public void scroll(double deltaX) {
    setStart((int) Math.rint(startProperty.get() - deltaX * decimateFactor));
  }

  public void zoom(double zoomFactor) {
    if (zoomFactor > 1) {
      zoomProperty.setValue(zoomProperty.get().next());
    }
    else if (zoomFactor < 1) {
      zoomProperty.setValue(zoomProperty.get().prev());
    }
  }

  public void checkLength(@Nonnegative int realDataLen) {
    if (realDataLen == 0) {
      setStart(0);
    }
    else {
      setStart(startProperty.get() + realDataLen - lengthProperty.get());
    }
  }

  public void preventCenter(@Nonnegative double width) {
    int prevChartCenter = startProperty.get() + lengthProperty.get() / 2;
    lengthProperty.setValue(SMALL.maxValue(width) * decimateFactor / stepProperty.get());
    setStart(prevChartCenter - lengthProperty.get() / 2);
  }

  public int getStart() {
    return startProperty.get();
  }

  public int getEnd() {
    return startProperty.get() + lengthProperty.get();
  }

  public int getDecimateFactor() {
    return decimateFactor;
  }

  private double getStep(@Nonnegative double frequency) {
    double pointsInSec = SMALL.getStep() * zoomProperty.get().mmPerSec / 10.0;
    decimateFactor = Math.max(1, (int) Math.rint(frequency / pointsInSec));
    double xStep = decimateFactor * pointsInSec / frequency;
    Logger.getLogger(getClass().getName()).log(Level.CONFIG,
        String.format("Frequency = %.0f Hz; x-zoom = %d mm/s; pixels per sec = %.1f; decimate factor = %d; x-step = %.1f px",
            frequency, zoomProperty.get().mmPerSec, pointsInSec, decimateFactor, xStep));
    return xStep;
  }

  private void setStart(int start) {
    startProperty.setValue(Math.max(0, start));
  }

  private void setStep(@Nonnegative double frequency) {
    stepProperty.set(getStep(frequency));
  }
}
