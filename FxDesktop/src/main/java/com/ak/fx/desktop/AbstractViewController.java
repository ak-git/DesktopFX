package com.ak.fx.desktop;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.Flow;
import java.util.concurrent.TimeUnit;
import java.util.function.IntConsumer;
import java.util.function.ObjIntConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.ak.comm.GroupService;
import com.ak.comm.converter.Variable;
import com.ak.comm.converter.Variables;
import com.ak.digitalfilter.FilterBuilder;
import com.ak.fx.scene.AxisXController;
import com.ak.fx.scene.AxisYController;
import com.ak.fx.scene.Chart;
import com.ak.fx.scene.ScaleYInfo;
import com.ak.fx.util.FxUtils;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;

public abstract class AbstractViewController<RESPONSE, REQUEST, EV extends Enum<EV> & Variable<EV>>
    implements Initializable, Flow.Subscriber<int[]> {
  @Nonnull
  private final GroupService<RESPONSE, REQUEST, EV> service;
  private final AxisXController axisXController = new AxisXController(new IntConsumer() {
    private final AnimationTimer timer = new AnimationTimer() {
      private long time = -1;

      @Override
      public void start() {
        super.start();
        time = -1;
      }

      @Override
      public void handle(long now) {
        if (time == -1) {
          time = now;
        }
        else if (now - time > TimeUnit.MILLISECONDS.toNanos(50)) {
          changed();
          stop();
        }
      }
    };
    private boolean posDirection;

    @Override
    public void accept(int shiftValue) {
      if (posDirection == (shiftValue > 0)) {
        if (Math.abs(shiftValue) > axisXController.getLength() / 2) {
          changed();
        }
        else {
          Logger.getLogger(getClass().getName()).log(Level.FINE, axisXController.toString());
          if (shiftValue > 0) {
            display(axisXController.getEnd(), shiftValue, (doubles, i) -> Objects.requireNonNull(chart).shiftRight(i, doubles));
          }
          else {
            display(axisXController.getStart(), shiftValue, (doubles, i) -> Objects.requireNonNull(chart).shiftLeft(i, doubles));
          }
          timer.start();
        }
      }
      else {
        posDirection = !posDirection;
        changed();
      }
    }

    private void display(@Nonnegative int axisEnd, int shiftValue, ObjIntConsumer<double[]> consumer) {
      List<? extends int[]> chartData = service.read(Variable.Option.VISIBLE, axisEnd - shiftValue, axisEnd);
      for (int i = 0; i < chartData.size(); i++) {
        consumer.accept(IntStream.of(filter(chartData.get(i))).parallel().
            mapToDouble(axisYController.getScale(service.getVariables(Variable.Option.VISIBLE).get(i))).toArray(), i);
      }
      check(shiftValue, chartData.get(0).length);
    }

    private void check(@Nonnegative int needSize, int shiftValue) {
      if (Math.abs(shiftValue) < needSize) {
        axisXController.checkLength(axisXController.getLength() - (needSize - shiftValue));
      }
    }
  });
  private final AxisYController<EV> axisYController = new AxisYController<>();
  @Nullable
  private Flow.Subscription subscription;
  @Nullable
  @FXML
  private Chart chart;

  public AbstractViewController(@Nonnull GroupService<RESPONSE, REQUEST, EV> service) {
    this.service = service;
  }

  @Override
  public final void initialize(@Nullable URL location, @Nullable ResourceBundle resources) {
    if (chart != null) {
      chart.setOnDragOver(event -> {
        Dragboard db = event.getDragboard();
        if (db.hasFiles()) {
          event.acceptTransferModes(TransferMode.COPY);
        }
        else {
          event.consume();
        }
      });
      chart.setOnDragDropped(event -> {
        Dragboard db = event.getDragboard();
        boolean ok = false;
        if (db.hasFiles()) {
          for (File file : db.getFiles()) {
            if (service.accept(file)) {
              ok = true;
              break;
            }
          }
        }
        event.setDropCompleted(ok);
        event.consume();
      });
      chart.getScene().addEventHandler(KeyEvent.KEY_RELEASED, event -> {
        if (KeyCombination.keyCombination("Shortcut+N").match(event)) {
          service.refresh();
        }
      });
      chart.setVariables(service.getVariables(Variable.Option.VISIBLE).stream().map(Variables::toString).collect(Collectors.toList()));
      chart.titleProperty().bind(axisXController.zoomProperty().asString());
      chart.setOnScroll(event -> {
        axisXController.scroll(event.getDeltaX());
        event.consume();
      });
      chart.setOnZoomStarted(event -> {
        axisXController.zoom(event.getZoomFactor());
        axisXController.preventEnd(chart.diagramWidthProperty().doubleValue());
        event.consume();
      });
      chart.diagramHeightProperty().addListener((observable, oldValue, newValue) -> {
        axisYController.setLineDiagramHeight(newValue.doubleValue());
        changed();
      });
      chart.diagramWidthProperty().addListener((observable, oldValue, newValue) -> axisXController.preventEnd(newValue.doubleValue()));
      axisXController.stepProperty().addListener((observable, oldValue, newValue) -> chart.setXStep(newValue.doubleValue()));
      axisXController.lengthProperty().addListener((observable, oldValue, newValue) ->
          chart.setMaxSamples(newValue.intValue() / axisXController.getDecimateFactor())
      );
      axisXController.setFrequency(service.getFrequency());
    }
    service.subscribe(this);
  }

  @Override
  public final void onSubscribe(@Nonnull Flow.Subscription s) {
    if (subscription != null) {
      subscription.cancel();
    }
    subscription = s;
    changed();
    subscription.request(axisXController.getLength());
  }

  @Override
  public final void onNext(@Nonnull int[] ints) {
  }

  @Override
  public final void onError(@Nonnull Throwable t) {
    Logger.getLogger(getClass().getName()).log(Level.WARNING, t.getMessage(), t);
  }

  @Override
  public final void onComplete() {
    changed();
  }

  private void changed() {
    Logger.getLogger(getClass().getName()).log(Level.FINE, axisXController.toString());
    List<? extends int[]> chartData = service.read(Variable.Option.VISIBLE, axisXController.getStart(), axisXController.getEnd());
    FxUtils.invokeInFx(() -> {
      IntStream.range(0, chartData.size()).forEachOrdered(i -> {
        int[] values = filter(chartData.get(i));
        ScaleYInfo<EV> scaleInfo = axisYController.scale(service.getVariables(Variable.Option.VISIBLE).get(i), values);
        Objects.requireNonNull(chart).setAll(i, IntStream.of(values).parallel().mapToDouble(scaleInfo).toArray(), scaleInfo);
      });
      axisXController.checkLength(chartData.get(0).length);
    });
  }

  private int[] filter(@Nonnull int[] input) {
    return FilterBuilder.of().sharpingDecimate(axisXController.getDecimateFactor()).filter(input);
  }
}
