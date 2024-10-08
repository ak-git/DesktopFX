package com.ak.fx.scene;

import com.ak.fx.stage.ScreenResolutionMonitor;
import com.ak.util.Numbers;
import javafx.scene.shape.Path;

import javax.annotation.Nonnegative;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.function.DoubleBinaryOperator;
import java.util.function.ToDoubleFunction;

enum GridCell implements GridCellCoordinate {
  POINTS(1.0) {
    private static final int FACTOR = 4;

    @Nonnegative
    @Override
    double getStep() {
      return super.getStep() / FACTOR;
    }

    @Override
    Path newPath() {
      var path = super.newPath();
      path.setStrokeDashOffset(getStep());

      path.getStrokeDashArray().addAll(0.0, getStep() * 2);
      for (var i = 0; i < FACTOR - 2; i++) {
        path.getStrokeDashArray().addAll(0.0, getStep());
      }
      return path;
    }
  },
  SMALL(1.0) {
    @Override
    @Nonnegative
    public double minCoordinate(@Nonnegative double size) {
      return defaultImplementation().minCoordinate(size);
    }

    @Override
    @Nonnegative
    public double maxValue(@Nonnegative double size) {
      return defaultImplementation().maxValue(size);
    }

    @Override
    @Nonnegative
    public double roundCoordinate(@Nonnegative double size) {
      return defaultImplementation().roundCoordinate(size);
    }
  },
  BIG(3.0) {
    @Nonnegative
    @Override
    double getStep() {
      return super.getStep() * 5.0;
    }
  };

  private final GridCellCoordinate defaultImplementation = new GridCellCoordinate() {
    @Override
    public double minCoordinate(double size) {
      double step = getStep();
      return size / 2.0 - Math.floor(size / 2.0 / step) * step;
    }

    @Override
    public double maxValue(double size) {
      double step = getStep();
      return Math.floor((size - minCoordinate(size)) / step) * step;
    }

    @Override
    public double roundCoordinate(double size) {
      double step = getStep();
      return Numbers.toInt(size / step) * step;
    }
  };

  @Nonnegative
  private final double strokeWidth;

  GridCell(@Nonnegative double strokeWidth) {
    this.strokeWidth = strokeWidth;
  }

  @Nonnegative
  final double getStrokeWidth() {
    return strokeWidth;
  }

  final GridCellCoordinate defaultImplementation() {
    return defaultImplementation;
  }

  @Nonnegative
  double getStep() {
    return ScreenResolutionMonitor.getDpi() / 2.54;
  }

  @Nonnegative
  final double linePad() {
    return (strokeWidth - 1.0) / 2.0;
  }

  Path newPath() {
    var p = new Path();
    p.setStroke(Colors.GRID_CELL);
    p.setStrokeWidth(getStrokeWidth());
    return p;
  }

  @Nonnegative
  @Override
  public double minCoordinate(@Nonnegative double size) {
    return doCoordinate(Math::max, value -> value.minCoordinate(size));
  }

  @Nonnegative
  @Override
  public double maxValue(@Nonnegative double size) {
    return doCoordinate(Math::min, value -> value.maxValue(size));
  }

  @Nonnegative
  @Override
  public double roundCoordinate(@Nonnegative double size) {
    return doCoordinate(Math::min, value -> value.roundCoordinate(size));
  }

  @Nonnegative
  private double doCoordinate(DoubleBinaryOperator action, ToDoubleFunction<GridCellCoordinate> coordinate) {
    return action.applyAsDouble(coordinate.applyAsDouble(defaultImplementation), coordinate.applyAsDouble(SMALL));
  }

  static List<Path> newPaths() {
    List<Path> paths = new LinkedList<>();
    EnumSet.allOf(GridCell.class).forEach(gridCell -> paths.add(gridCell.newPath()));
    return Collections.unmodifiableList(paths);
  }

  static double mmToScreen(int value) {
    return value * SMALL.getStep() / 10.0;
  }

  static int mm(double value) {
    return Numbers.toInt(value / (SMALL.getStep() / 10.0));
  }
}

interface GridCellCoordinate {
  @Nonnegative
  double minCoordinate(@Nonnegative double size);

  @Nonnegative
  double maxValue(@Nonnegative double size);

  @Nonnegative
  double roundCoordinate(@Nonnegative double size);
}
