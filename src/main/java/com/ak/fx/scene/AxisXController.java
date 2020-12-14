package com.ak.fx.scene;

import java.util.EnumSet;
import java.util.function.DoublePredicate;
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
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import tec.uom.se.quantity.Quantities;
import tec.uom.se.unit.MetricPrefix;
import tec.uom.se.unit.Units;

import static com.ak.fx.scene.GridCell.SMALL;

public final class AxisXController {
  private enum ZoomX {
    Z_10(10), Z_25(25), Z_50(50);

    @Nonnegative
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

  private enum ZoomXEvent implements DoublePredicate {
    ZOOM_OUT {
      @Override
      public boolean test(double value) {
        return value < 1.0;
      }
    }, ZOOM_IN {
      @Override
      public boolean test(double value) {
        return value > 1.0;
      }
    }, ZOOM_STOP {
      @Override
      public boolean test(double value) {
        return true;
      }
    };

    static ZoomXEvent find(double zoom) {
      return EnumSet.allOf(ZoomXEvent.class).stream().filter(zoomXEvent -> zoomXEvent.test(zoom)).findFirst().orElse(ZOOM_STOP);
    }
  }

  private final IntegerProperty startProperty = new SimpleIntegerProperty();
  private final IntegerProperty lengthProperty = new SimpleIntegerProperty();
  private final ObjectProperty<ZoomX> zoomProperty = new SimpleObjectProperty<>(ZoomX.Z_25);
  private final ObjectProperty<ZoomXEvent> zoomEvent = new SimpleObjectProperty<>(ZoomXEvent.ZOOM_STOP);
  private final DoubleProperty stepProperty = new SimpleDoubleProperty();
  @Nonnegative
  private int decimateFactor = 1;

  public AxisXController(@Nonnull Runnable onUpdate) {
    startProperty.addListener((observable, oldValue, newValue) -> onUpdate.run());
    lengthProperty.addListener((observable, oldValue, newValue) -> onUpdate.run());
    zoomEvent.addListener((observable, oldValue, newValue) -> {
      if (oldValue == ZoomXEvent.ZOOM_IN) {
        zoomProperty.setValue(zoomProperty.get().next());
      }
      else if (oldValue == ZoomXEvent.ZOOM_OUT) {
        zoomProperty.setValue(zoomProperty.get().prev());
      }
    });
  }

  @Override
  public String toString() {
    return "axis-x size = %d [%d - %d]; x-zoom = %d mm/s; decimate factor = %d".
        formatted(getLength(), getStart(), getEnd(), zoomProperty.get().mmPerSec, decimateFactor);
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

  public ReadOnlyIntegerProperty lengthProperty() {
    return lengthProperty;
  }

  public void scroll(double deltaX) {
    setStart(toInt(getStart() - deltaX * decimateFactor));
  }

  public void zoom(double zoomFactor) {
    zoomEvent.setValue(ZoomXEvent.find(zoomFactor));
  }

  public void checkLength(@Nonnegative int realDataLen) {
    if (realDataLen == 0) {
      setStart(0);
    }
    else {
      setStart(getStart() + realDataLen - getLength());
    }
  }

  public void preventEnd(@Nonnegative double width) {
    int newLen = toInt(width / stepProperty.get()) * decimateFactor;
    setStart(getEnd() - newLen);
    lengthProperty.set(newLen);
  }

  public int getStart() {
    return startProperty.get();
  }

  public int getLength() {
    return lengthProperty.get();
  }

  public int getEnd() {
    return getStart() + getLength();
  }

  public int getDecimateFactor() {
    return decimateFactor;
  }

  private double getStep(@Nonnegative double frequency) {
    double pointsInSec = SMALL.getStep() * zoomProperty.get().mmPerSec / 10.0;
    decimateFactor = Math.max(1, toInt(frequency / pointsInSec));
    double xStep = decimateFactor * pointsInSec / frequency;
    Logger.getLogger(getClass().getName()).log(Level.CONFIG,
        () -> "frequency = %.0f Hz; x-zoom = %d mm/s; pixels per sec = %.1f; decimate factor = %d; x-step = %.1f px"
            .formatted(frequency, zoomProperty.get().mmPerSec, pointsInSec, decimateFactor, xStep));
    return xStep;
  }

  private void setStart(int start) {
    startProperty.setValue(Math.max(0, toInt(start / (decimateFactor * 1.0)) * decimateFactor));
  }

  private void setStep(@Nonnegative double frequency) {
    stepProperty.set(getStep(frequency));
  }

  private static int toInt(double d) {
    return (int) Math.rint(d);
  }
}
