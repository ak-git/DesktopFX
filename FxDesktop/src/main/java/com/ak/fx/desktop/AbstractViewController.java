package com.ak.fx.desktop;

import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.Flow;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
import com.ak.util.Strings;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
import javafx.util.Duration;

public abstract class AbstractViewController<T, R, V extends Enum<V> & Variable<V>>
    implements Initializable, Flow.Subscriber<int[]> {
  @Nonnull
  private final GroupService<T, R, V> service;
  private final AxisXController axisXController = new AxisXController(this::changed);
  private final AxisYController<V> axisYController = new AxisYController<>();
  @Nullable
  private Flow.Subscription subscription;
  @Nullable
  @FXML
  private Chart chart;

  public AbstractViewController(@Nonnull GroupService<T, R, V> service) {
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
      chart.setVariables(service.getVariables().stream().filter(v -> v.options().contains(Variable.Option.VISIBLE))
          .map(Variables::toString).collect(Collectors.toList()));
      chart.titleProperty().bind(axisXController.zoomProperty().asString());
      chart.setOnScroll(event -> {
        axisXController.scroll(event.getDeltaX());
        event.consume();
      });
      chart.setOnZoomStarted(event -> {
        axisXController.zoom(event.getZoomFactor());
        axisXController.preventEnd(chart.diagramWidthProperty().doubleValue());
        changed();
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

      Timeline timeline = new Timeline();
      timeline.getKeyFrames().add(new KeyFrame(Duration.millis(100), (ActionEvent actionEvent) -> axisXController.scroll(-1000)));
      timeline.setCycleCount(Animation.INDEFINITE);
      SequentialTransition animation;
      animation = new SequentialTransition();
      animation.getChildren().addAll(timeline);
      animation.play();
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
    FxUtils.invokeInFx(() -> Objects.requireNonNull(chart).setBannerText(
        service.getVariables().stream().filter(v -> v.options().contains(Variable.Option.TEXT_VALUE_BANNER))
            .map(v -> Variables.toString(v, ints[v.ordinal()])).collect(Collectors.joining(Strings.NEW_LINE_2)))
    );
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
    Logger.getLogger(getClass().getName()).log(Level.FINE, axisXController::toString);
    Map<V, int[]> chartData = service.read(axisXController.getStart(), axisXController.getEnd());
    FxUtils.invokeInFx(() -> {
      chartData.forEach((v, ints) -> {
        if (v.options().contains(Variable.Option.VISIBLE)) {
          int[] values = FilterBuilder.of().sharpingDecimate(axisXController.getDecimateFactor()).filter(ints);
          ScaleYInfo<V> scaleInfo = axisYController.scale(v, values);
          Objects.requireNonNull(chart).setAll(v.indexBy(Variable.Option.VISIBLE), IntStream.of(values).unordered().parallel()
              .mapToDouble(scaleInfo).toArray(), scaleInfo);
        }
      });
      axisXController.checkLength(chartData.values().iterator().next().length);
    });
  }
}