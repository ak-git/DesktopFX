package com.ak.fx.scene;

import com.ak.fx.stage.ScreenResolutionMonitor;
import javafx.application.Platform;
import javafx.scene.layout.Pane;
import javafx.scene.shape.*;

import javax.annotation.Nonnegative;
import java.util.List;

final class MilliGrid extends Pane {
  private final GridLine[] gridLines = {new HorizontalGridLine(), new VerticalGridLine()};
  private List<Path> paths = GridCell.newPaths();

  MilliGrid() {
    reinitializePaths();
    ScreenResolutionMonitor.dpi(this::getScene).addListener(
        (observable, oldValue, newValue) -> Platform.runLater(this::reinitializePaths)
    );
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
    void addToPath(Path path, GridCell gridCell);

    @Nonnegative
    double contentSize();

    PathElement moveTo(@Nonnegative double c, GridCell gridCell);

    PathElement lineTo(GridCell gridCell);
  }

  private abstract static class AbstractGridLine implements GridLine {
    @Override
    public final void addToPath(Path path, GridCell gridCell) {
      double contentSize = contentSize();
      int factor = (int) Math.round(GridCell.SMALL.getStep() / gridCell.getStep());
      var i = 0;
      for (double c = gridCell.minCoordinate(contentSize); c < maxValue(contentSize) + 1.0; c += gridCell.getStep()) {
        if (!(gridCell == GridCell.POINTS && i % factor == 0)) {
          path.getElements().addAll(moveTo(c, gridCell), lineTo(gridCell));
        }
        i++;
      }
    }

    final double lineToCoordinate(GridCell gridCell) {
      return maxValue(length()) - gridCell.linePad();
    }

    @Nonnegative
    abstract double length();

    @Nonnegative
    private static double maxValue(@Nonnegative double size) {
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
    public PathElement moveTo(@Nonnegative double x, GridCell gridCell) {
      return new MoveTo(snappedLeftInset() + x,
          snappedTopInset() + GridCell.SMALL.minCoordinate(length()) + gridCell.linePad());
    }

    @Override
    public PathElement lineTo(GridCell gridCell) {
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
    public PathElement moveTo(@Nonnegative double y, GridCell gridCell) {
      return new MoveTo(snappedLeftInset() + GridCell.SMALL.minCoordinate(length()) + gridCell.linePad(),
          snappedTopInset() + y);
    }

    @Override
    public PathElement lineTo(GridCell gridCell) {
      return new HLineTo(snappedLeftInset() + lineToCoordinate(gridCell));
    }

    @Nonnegative
    @Override
    double length() {
      return getWidth();
    }
  }
}
