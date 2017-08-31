package com.ak.fx.scene;

import java.util.List;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.fx.stage.ScreenResolutionMonitor;
import javafx.application.Platform;
import javafx.scene.layout.Pane;
import javafx.scene.shape.HLineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.VLineTo;

final class MilliGrid extends Pane {
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
      for (double c = gridCell.minCoordinate(contentSize); c < maxWidth(contentSize) + 1.0; c += gridCell.getStep()) {
        if (!(gridCell == GridCell.POINTS && i % factor == 0)) {
          path.getElements().addAll(moveTo(c, gridCell), lineTo(gridCell));
        }
        i++;
      }
    }

    final double lineToCoordinate(@Nonnull GridCell gridCell) {
      return maxWidth(length()) - gridCell.linePad();
    }

    @Nonnegative
    abstract double length();

    @Nonnegative
    private static double maxWidth(@Nonnegative double size) {
      return size - GridCell.SMALL.minCoordinate(size);
    }
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
