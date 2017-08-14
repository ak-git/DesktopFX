package com.ak.fx.scene;

import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnegative;

import com.ak.fx.stage.ScreenResolutionMonitor;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Path;

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
      Path path = super.newPath();
      path.setStrokeDashOffset(getStep());

      path.getStrokeDashArray().addAll(0.0, getStep() * 2);
      for (int i = 0; i < FACTOR - 2; i++) {
        path.getStrokeDashArray().addAll(0.0, getStep());
      }
      return path;
    }
  },
  SMALL(1.0),
  BIG(3.0) {
    @Nonnegative
    @Override
    double getStep() {
      return super.getStep() * 5.0;
    }
  };

  private static final Paint COLOR = new Color(225.0 / 255.0, 130.0 / 255.0, 110.0 / 255.0, 1.0);
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
    return ScreenResolutionMonitor.INSTANCE.getDpi() / 2.54;
  }

  @Nonnegative
  double linePad() {
    return (strokeWidth - 1.0) / 2.0;
  }

  Path newPath() {
    Path p = new Path();
    p.setStroke(COLOR);
    p.setStrokeWidth(getStrokeWidth());
    return p;
  }

  @Nonnegative
  double minCoordinate(@Nonnegative double size) {
    double min = size / 2.0 - Math.floor(size / 2 / getStep()) * getStep();
    if (this != SMALL) {
      min = Math.max(min, SMALL.minCoordinate(size));
    }
    return min;
  }

  @Nonnegative
  double maxCoordinate(@Nonnegative double size) {
    return Math.floor((size - minCoordinate(size)) / getStep()) * getStep();
  }

  static List<Path> newPaths() {
    List<Path> paths = new LinkedList<>();
    EnumSet.allOf(GridCell.class).forEach(gridCell -> paths.add(gridCell.newPath()));
    return Collections.unmodifiableList(paths);
  }
}
