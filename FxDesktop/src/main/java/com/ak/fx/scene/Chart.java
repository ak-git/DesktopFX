package com.ak.fx.scene;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.comm.converter.Variable;
import com.ak.comm.converter.Variables;
import com.ak.digitalfilter.FilterBuilder;
import com.ak.digitalfilter.Filters;
import com.ak.fx.util.FxUtils;
import com.ak.util.Strings;
import javafx.scene.text.Text;

import static com.ak.fx.scene.GridCell.BIG;
import static com.ak.fx.scene.GridCell.POINTS;
import static com.ak.fx.scene.GridCell.SMALL;

public final class Chart<EV extends Enum<EV> & Variable<EV>> extends AbstractRegion {
  private static final double[] EMPTY_DOUBLES = {};
  private final MilliGrid milliGrid = new MilliGrid();
  private final List<LineDiagram> lineDiagrams = new ArrayList<>();
  private final Text xAxisUnit = new Text();
  private final AxisXController axisXController = new AxisXController(this::changed);
  private final AxisYController<EV> axisYController = new AxisYController<>();
  @Nonnull
  private BiFunction<Integer, Integer, List<? extends int[]>> dataCallback = (start, end) -> Collections.emptyList();

  public Chart() {
    milliGrid.setManaged(false);
    getChildren().add(milliGrid);
    xAxisUnit.fontProperty().bind(Fonts.H2.fontProperty());
  }

  public void setVariables(@Nonnull Collection<EV> variables, @Nonnegative double frequency) {
    lineDiagrams.addAll(variables.stream().map(ev -> new LineDiagram(Variables.toString(ev))).collect(Collectors.toList()));
    lineDiagrams.forEach(lineDiagram -> lineDiagram.setManaged(false));
    axisXController.setFrequency(frequency, xStep -> lineDiagrams.forEach(lineDiagram -> lineDiagram.setXStep(xStep)));
    axisYController.setVariables(variables);

    getChildren().addAll(lineDiagrams);
    getChildren().add(xAxisUnit);

    xAxisUnit.textProperty().bind(axisXController.zoomProperty().asString());
    setOnScroll(event -> {
      axisXController.scroll(event.getDeltaX());
      event.consume();
    });
    setOnScrollFinished(event -> {
      Logger.getLogger(getClass().getName()).log(Level.CONFIG, axisXController.toString());
      event.consume();
    });
    setOnZoomStarted(event -> {
      axisXController.zoom(event.getZoomFactor());
      event.consume();
    });
    heightProperty().addListener((observable, oldValue, newValue) -> changed());
  }

  public void setDataCallback(@Nonnull BiFunction<Integer, Integer, List<? extends int[]>> dataCallback) {
    this.dataCallback = dataCallback;
  }

  public void changed() {
    Logger.getLogger(getClass().getName()).log(Level.FINE, axisXController.toString());
    setAll(dataCallback.apply(axisXController.getStart(), axisXController.getEnd()));
  }

  private void setAll(@Nonnull List<? extends int[]> chartData) {
    FxUtils.invokeInFx(() -> {
      if (chartData.isEmpty()) {
        lineDiagrams.forEach(lineDiagram -> lineDiagram.setAll(EMPTY_DOUBLES, value -> Strings.EMPTY));
        axisXController.reset();
      }
      else {
        IntStream.range(0, chartData.size()).forEachOrdered(i -> {
          int[] values = Filters.filter(FilterBuilder.of().sharpingDecimate(axisXController.getDecimateFactor()).build(), chartData.get(i));
          axisYController.scaleOrdered(values, scaleInfo ->
              lineDiagrams.get(i).setAll(IntStream.of(values).parallel().mapToDouble(scaleInfo).toArray(), scaleInfo));
        });
        axisXController.checkLength(chartData.get(0).length);
      }
    });
  }

  @Override
  void layoutAll(double x, double y, double width, double height) {
    milliGrid.resizeRelocate(x, y, width, height);
    layoutLineDiagrams(x + SMALL.minCoordinate(width), y + SMALL.minCoordinate(height), SMALL.maxValue(width), SMALL.maxValue(height));
    axisXController.preventCenter(SMALL.maxValue(width));
  }

  private void layoutLineDiagrams(double x, double y, double width, double height) {
    xAxisUnit.relocate(x + BIG.minCoordinate(width) + BIG.maxValue(width) / 2 + POINTS.getStep(),
        y + SMALL.getStep() / 2 - xAxisUnit.getFont().getSize());

    double dHeight = SMALL.maxValue((height + POINTS.getStep()) * 2 / (1 + lineDiagrams.size()));
    if (dHeight >= SMALL.getStep() * 2) {
      axisYController.setLineDiagramHeight(dHeight);
      lineDiagrams.forEach(lineDiagram -> lineDiagram.resizeRelocate(x, y, width, dHeight));
      for (int i = 0; i < lineDiagrams.size() / 2; i++) {
        lineDiagrams.get(i).relocate(x, y + SMALL.roundCoordinate(height / (lineDiagrams.size() + 1)) * i);
      }
      if ((lineDiagrams.size() & 1) != 0) {
        lineDiagrams.get(lineDiagrams.size() / 2).relocate(x, y + height / 2 - dHeight / 2);
      }
      for (int i = 0; i < lineDiagrams.size() / 2; i++) {
        lineDiagrams.get(lineDiagrams.size() - 1 - i).
            relocate(x, y + height - dHeight - SMALL.roundCoordinate(height / (lineDiagrams.size() + 1)) * i);
      }
    }
    else {
      axisYController.setLineDiagramHeight(height);
      lineDiagrams.forEach(lineDiagram -> lineDiagram.resizeRelocate(x, y, width, height));
    }
  }
}
