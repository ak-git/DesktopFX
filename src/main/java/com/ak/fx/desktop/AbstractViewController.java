package com.ak.fx.desktop;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.Flow;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.annotation.ParametersAreNullableByDefault;

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
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.input.ZoomEvent;
import javafx.util.Duration;

abstract class AbstractViewController<T, R, V extends Enum<V> & Variable<V>>
    implements Initializable, Flow.Subscriber<int[]>, AutoCloseable, ViewController {
  @Nonnull
  private final GroupService<T, R, V> service;
  private final AxisXController axisXController = new AxisXController(this::changed);
  private final AxisYController<V> axisYController = new AxisYController<>();
  @Nullable
  private Flow.Subscription subscription;
  @Nullable
  @FXML
  private Chart chart;

  AbstractViewController(@Nonnull GroupService<T, R, V> service) {
    this.service = service;
  }

  @Override
  @ParametersAreNullableByDefault
  public final void initialize(URL location, ResourceBundle resources) {
    if (chart != null) {
      chart.setOnDragOver(event -> {
        if (event.getDragboard().hasFiles()) {
          event.acceptTransferModes(TransferMode.COPY);
        }
        else {
          event.consume();
        }
      });
      chart.setOnDragDropped(event -> {
        event.setDropCompleted(event.getDragboard().getFiles().stream().anyMatch(service::accept));
        event.consume();
      });
      chart.setVariables(service.getVariables().stream().filter(v -> v.options().contains(Variable.Option.VISIBLE))
          .map(Variables::toString).collect(Collectors.toList()));
      chart.titleProperty().bind(axisXController.zoomProperty().asString());
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

    Timeline timeline = new Timeline();
    timeline.getKeyFrames().add(new KeyFrame(Duration.millis(50), (ActionEvent actionEvent) -> axisXController.scroll(-500)));
    timeline.setCycleCount(Animation.INDEFINITE);
    SequentialTransition animation = new SequentialTransition();
    animation.getChildren().addAll(timeline);
    animation.play();
    service.subscribe(this);
  }

  @Override
  @OverridingMethodsMustInvokeSuper
  public void onSubscribe(@Nonnull Flow.Subscription s) {
    if (subscription != null) {
      subscription.cancel();
    }
    subscription = s;
    changed();
    subscription.request(axisXController.getLength());
  }

  @Override
  @OverridingMethodsMustInvokeSuper
  public void onNext(@Nonnull int[] ints) {
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

  @Override
  @OverridingMethodsMustInvokeSuper
  public void close() {
    service.close();
  }

  @Override
  @OverridingMethodsMustInvokeSuper
  public void refresh() {
    service.refresh();
    changed();
  }

  @Override
  public final void zoom(ZoomEvent event) {
    if (chart != null) {
      axisXController.zoom(event.getZoomFactor());
      axisXController.preventEnd(chart.diagramWidthProperty().doubleValue());
      changed();
    }
  }

  @Override
  public final void scroll(ScrollEvent event) {
    axisXController.scroll(event.getDeltaX());
  }

  @Nonnull
  protected final GroupService<T, R, V> service() {
    return service;
  }

  private void changed() {
    Logger.getLogger(getClass().getName()).log(Level.FINE, axisXController::toString);
    int[][] chartData = service.read(axisXController.getStart(), axisXController.getEnd());
    FxUtils.invokeInFx(() -> {
      for (V v : service.getVariables()) {
        if (v.options().contains(Variable.Option.VISIBLE)) {
          int[] values = FilterBuilder.of().sharpingDecimate(axisXController.getDecimateFactor()).filter(chartData[v.ordinal()]);
          ScaleYInfo<V> scaleInfo = axisYController.scale(v, values);
          Objects.requireNonNull(chart).setAll(v.indexBy(Variable.Option.VISIBLE), IntStream.of(values).unordered().parallel()
              .mapToDouble(scaleInfo).toArray(), scaleInfo);
        }
      }
      if (chartData[0].length > 0) {
        onNext(service.getVariables().stream().mapToInt(
            e -> {
              int[] ints = chartData[e.ordinal()];
              return ints[ints.length - 1];
            }).toArray()
        );
      }
      axisXController.checkLength(chartData[0].length);
    });
  }
}