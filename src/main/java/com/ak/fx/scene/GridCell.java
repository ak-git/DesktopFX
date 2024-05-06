package com.ak.fx.scene;

import com.ak.fx.stage.ScreenResolutionMonitor;
import com.ak.util.Numbers;
import javafx.scene.shape.Path;

import javax.annotation.Nonnegative;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

enum GridCell {
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
    double minCoordinate(@Nonnegative double size) {
      return GridCell.minCoordinate(getStep(), size);
    }

    @Override
    @Nonnegative
    double maxValue(@Nonnegative double size) {
      return GridCell.maxValue(getStep(), size);
    }

    @Override
    @Nonnegative
    double roundCoordinate(@Nonnegative double size) {
      return GridCell.roundCoordinate(getStep(), size);
    }
  },
  BIG(3.0) {
    @Nonnegative
    @Override
    double getStep() {
      return super.getStep() * 5.0;
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
  double minCoordinate(@Nonnegative double size) {
    return Math.max(minCoordinate(getStep(), size), SMALL.minCoordinate(size));
  }

  @Nonnegative
  double maxValue(@Nonnegative double size) {
    return Math.min(maxValue(getStep(), size), SMALL.maxValue(size));
  }

  @Nonnegative
  double roundCoordinate(@Nonnegative double size) {
    return Math.min(roundCoordinate(getStep(), size), SMALL.roundCoordinate(size));
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

  @Nonnegative
  private static double minCoordinate(@Nonnegative double step, @Nonnegative double size) {
    return size / 2.0 - Math.floor(size / 2.0 / step) * step;
  }

  @Nonnegative
  private static double maxValue(@Nonnegative double step, @Nonnegative double size) {
    return Math.floor((size - minCoordinate(step, size)) / step) * step;
  }

  @Nonnegative
  private static double roundCoordinate(@Nonnegative double step, @Nonnegative double size) {
    return Numbers.toInt(size / step) * step;
  }
}
