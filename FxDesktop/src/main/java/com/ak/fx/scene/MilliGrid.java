package com.ak.fx.scene;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

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

public final class MilliGrid extends Pane {
  private enum GridCell {
    POINTS(1.0) {
      private static final int FACTOR = 4;

      @Nonnegative
      @Override
      double getStep() {
        return super.getStep() / FACTOR;
      }

      @Nonnull
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

    @Nonnull
    Path newPath() {
      Path p = new Path();
      p.setStroke(COLOR);
      p.setStrokeWidth(getStrokeWidth());
      return p;
    }

    @Nonnull
    static Map<GridCell, Path> newPaths() {
      Map<GridCell, Path> map = new EnumMap<>(GridCell.class);
      EnumSet.allOf(GridCell.class).forEach(gridCell -> map.put(gridCell, gridCell.newPath()));
      return Collections.unmodifiableMap(map);
    }
  }

  private final GridLine[] gridLines = {new HorizontalGridLine(), new VerticalGridLine()};
  @Nonnull
  private Map<GridCell, Path> paths = Collections.emptyMap();

  public MilliGrid() {
    ScreenResolutionMonitor.INSTANCE.getDpiObservable().subscribe(dpi -> {
      Platform.runLater(() -> {
        reinitializePaths();
        requestLayout();
      });
    });
  }

  @Override
  protected void layoutChildren() {
    paths.values().forEach(path -> path.getElements().clear());

    for (GridCell gridCell : paths.keySet()) {
      for (GridLine gridLine : gridLines) {
        gridLine.addToPath(paths.get(gridCell), gridCell);
        if (gridCell == GridCell.POINTS) {
          break;
        }
      }
    }
    super.layoutChildren();
  }

  private void reinitializePaths() {
    getChildren().removeAll(paths.values());
    paths = GridCell.newPaths();
    getChildren().addAll(paths.values());
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

  private abstract class AbstractGridLine implements GridLine {
    @Override
    public final void addToPath(@Nonnull Path path, @Nonnull GridCell gridCell) {
      double contentSize = contentSize();
      int factor = (int) Math.round(GridCell.SMALL.getStep() / gridCell.getStep());
      int i = 0;
      for (double c = minCoordinate(contentSize, gridCell); c < maxCoordinate(contentSize) + 1.0; c += gridCell.getStep()) {
        if (!(gridCell == GridCell.POINTS && i % factor == 0)) {
          path.getElements().addAll(moveTo(c, gridCell), lineTo(gridCell));
        }
        i++;
      }
    }

    @Nonnegative
    final double contentWidth() {
      return snapSize(getWidth() - (snappedLeftInset() + snappedRightInset()));
    }

    @Nonnegative
    final double contentHeight() {
      return snapSize(getHeight() - (snappedTopInset() + snappedBottomInset()));
    }

    @Nonnegative
    final double minCoordinate(@Nonnegative double size, @Nonnull GridCell gridCell) {
      double min = size / 2.0 - Math.floor(size / 2 / gridCell.getStep()) * gridCell.getStep();
      if (gridCell != GridCell.SMALL) {
        min = Math.max(min, minCoordinate(size, GridCell.SMALL));
      }
      return min;
    }

    @Nonnegative
    final double maxCoordinate(@Nonnegative double size) {
      return size - minCoordinate(size, GridCell.SMALL);
    }

    @Nonnegative
    final double linePad(@Nonnull GridCell gridCell) {
      return (gridCell.getStrokeWidth() - 1.0) / 2.0;
    }
  }

  private final class VerticalGridLine extends AbstractGridLine {
    @Nonnegative
    @Override
    public double contentSize() {
      return contentWidth();
    }

    @Nonnull
    @Override
    public PathElement moveTo(@Nonnegative double x, @Nonnull GridCell gridCell) {
      return new MoveTo(snappedLeftInset() + x, snappedTopInset() + minCoordinate(contentHeight(), GridCell.SMALL) + linePad(gridCell));
    }

    @Nonnull
    @Override
    public PathElement lineTo(@Nonnull GridCell gridCell) {
      return new VLineTo(snappedTopInset() + maxCoordinate(contentHeight()) - linePad(gridCell));
    }
  }

  private final class HorizontalGridLine extends AbstractGridLine {
    @Nonnegative
    @Override
    public double contentSize() {
      return contentHeight();
    }

    @Nonnull
    @Override
    public PathElement moveTo(@Nonnegative double y, @Nonnull GridCell gridCell) {
      return new MoveTo(snappedLeftInset() + minCoordinate(contentWidth(), GridCell.SMALL) + linePad(gridCell), snappedTopInset() + y);
    }

    @Nonnull
    @Override
    public PathElement lineTo(@Nonnull GridCell gridCell) {
      return new HLineTo(snappedLeftInset() + maxCoordinate(contentWidth()) - linePad(gridCell));
    }
  }
}
