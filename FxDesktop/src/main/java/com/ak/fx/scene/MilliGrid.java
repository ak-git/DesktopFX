package com.ak.fx.scene;

import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.fx.stage.ScreenResolutionMonitor;
import javafx.application.Platform;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.HLineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.VLineTo;

final class MilliGrid extends Pane {
  private enum GridCell {
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
    static double maxCoordinate(@Nonnegative double size) {
      return size - SMALL.minCoordinate(size);
    }

    static List<Path> newPaths() {
      List<Path> paths = new LinkedList<>();
      EnumSet.allOf(GridCell.class).forEach(gridCell -> paths.add(gridCell.newPath()));
      return Collections.unmodifiableList(paths);
    }
  }

  private final GridLine[] gridLines = {new HorizontalGridLine(), new VerticalGridLine()};
  @Nonnull
  private List<Path> paths = GridCell.newPaths();

  MilliGrid() {
    reinitializePaths();
    ScreenResolutionMonitor.INSTANCE.dpi().addListener((observable, oldValue, newValue) -> Platform.runLater(this::reinitializePaths));
  }

  @Override
  protected void layoutChildren() {
    paths.forEach(path -> path.getElements().clear());

    for (GridCell gridCell : GridCell.values()) {
      for (GridLine gridLine : gridLines) {
        gridLine.addToPath(paths.get(gridCell.ordinal()), gridCell);
        if (gridCell == GridCell.POINTS) {
          break;
        }
      }
    }
    super.layoutChildren();
  }

  private void reinitializePaths() {
    getChildren().removeAll(paths);
    paths = GridCell.newPaths();
    getChildren().addAll(paths);
    requestLayout();
  }

  private interface GridLine {
    void addToPath(@Nonnull Path path, @Nonnull GridCell gridCell);

    @Nonnegative
    double contentSize();

    @Nonnull
    PathElement moveTo(@Nonnegative double c, @Nonnull GridCell gridCell);

    @Nonnull
    PathElement lineTo(@Nonnull GridCell gridCell);
  }

  private abstract static class AbstractGridLine implements GridLine {
    @Override
    public final void addToPath(@Nonnull Path path, @Nonnull GridCell gridCell) {
      double contentSize = contentSize();
      int factor = (int) Math.round(GridCell.SMALL.getStep() / gridCell.getStep());
      int i = 0;
      for (double c = gridCell.minCoordinate(contentSize); c < GridCell.maxCoordinate(contentSize) + 1.0; c += gridCell.getStep()) {
        if (!(gridCell == GridCell.POINTS && i % factor == 0)) {
          path.getElements().addAll(moveTo(c, gridCell), lineTo(gridCell));
        }
        i++;
      }
    }

    final double lineToCoordinate(@Nonnull GridCell gridCell) {
      return GridCell.maxCoordinate(length()) - gridCell.linePad();
    }

    @Nonnegative
    abstract double length();
  }

  private final class VerticalGridLine extends AbstractGridLine {
    @Nonnegative
    @Override
    public double contentSize() {
      return getWidth();
    }

    @Override
    public PathElement moveTo(@Nonnegative double x, @Nonnull GridCell gridCell) {
      return new MoveTo(snappedLeftInset() + x,
          snappedTopInset() + GridCell.SMALL.minCoordinate(length()) + gridCell.linePad());
    }

    @Override
    public PathElement lineTo(@Nonnull GridCell gridCell) {
      return new VLineTo(snappedTopInset() + lineToCoordinate(gridCell));
    }

    @Nonnegative
    @Override
    double length() {
      return getHeight();
    }
  }

  private final class HorizontalGridLine extends AbstractGridLine {
    @Nonnegative
    @Override
    public double contentSize() {
      return getHeight();
    }

    @Override
    public PathElement moveTo(@Nonnegative double y, @Nonnull GridCell gridCell) {
      return new MoveTo(snappedLeftInset() + GridCell.SMALL.minCoordinate(length()) + gridCell.linePad(),
          snappedTopInset() + y);
    }

    @Override
    public PathElement lineTo(@Nonnull GridCell gridCell) {
      return new HLineTo(snappedLeftInset() + lineToCoordinate(gridCell));
    }

    @Nonnegative
    @Override
    double length() {
      return getWidth();
    }
  }
}
