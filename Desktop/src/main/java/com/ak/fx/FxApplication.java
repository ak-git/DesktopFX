package com.ak.fx;

import com.ak.fx.desktop.ViewController;
import com.ak.fx.scene.Colors;
import com.ak.fx.scene.Fonts;
import com.ak.fx.storage.OSStageStorage;
import com.ak.fx.storage.SplitPaneStorage;
import com.ak.fx.storage.Storage;
import com.ak.fx.util.FxUtils;
import com.ak.fx.util.OSDockImage;
import com.ak.util.OS;
import com.ak.util.Strings;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventTarget;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FxApplication extends Application implements ViewController {
  private static final String KEY_PROPERTIES = "keys";
  private static final String KEY_APPLICATION_TITLE = "application.title";
  private static final String KEY_APPLICATION_IMAGE = "application.image";

  @Override
  public final void start(Stage mainStage) throws IOException {
    var resourceBundle = ResourceBundle.getBundle(
        String.join(".", FxApplication.class.getPackageName(), KEY_PROPERTIES));
    List<FXMLLoader> fxmlLoaders = getFXMLLoader(resourceBundle);
    OSDockImage.valueOf(OS.get().name()).setIconImage(mainStage,
        Objects.requireNonNull(FxApplication.class.getResource(resourceBundle.getString(KEY_APPLICATION_IMAGE)))
    );

    var root = new SplitPane();
    root.setOrientation(Orientation.VERTICAL);
    for (FXMLLoader fxmlLoader : fxmlLoaders) {
      root.getItems().add(fxmlLoader.load());
    }
    Storage<SplitPane> dividerStorage = new SplitPaneStorage(FxApplication.class,
        fxmlLoaders.stream()
            .map(fxmlLoader -> fxmlLoader.getController().getClass().getSimpleName())
            .collect(Collectors.joining())
    );
    root.getDividers()
        .forEach(divider -> divider.positionProperty()
            .addListener((_, _, _) -> dividerStorage.save(root))
        );
    root.setOnDragOver(event -> {
      if (event.getDragboard().hasFiles()) {
        event.acceptTransferModes(TransferMode.COPY);
      }
      else {
        event.consume();
      }
    });

    var stage = new Stage(StageStyle.DECORATED);
    StackPane logo = new StackPane();
    stage.setScene(new Scene(logo, 1024, 768));
    stage.setTitle(resourceBundle.getString(KEY_APPLICATION_TITLE));
    stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
    stage.getScene().setOnZoomFinished(event -> {
      zoom(event.getTotalZoomFactor());
      event.consume();
    });
    stage.getScene().setOnScroll(event -> {
      scroll(event.getDeltaX());
      event.consume();
    });

    Text subHeader = new Text("alexander kobelev");
    subHeader.setFill(Colors.GRID_CELL);
    Text header = new Text("ak");
    header.setFill(Colors.GRID_CELL);
    header.fontProperty().addListener((_, _, _) -> {
      logo.getChildren().clear();
      double radius = Stream.of(header, subHeader)
          .mapToDouble(value -> value.getBoundsInLocal().getWidth()).summaryStatistics().getMax() * 1.2 / 2.0 + 3.0;
      Arc arc = new Arc(0, 0,
          radius,
          radius, 0, 360);
      arc.setStroke(Colors.GRID_CELL);
      arc.setStrokeWidth(3.0);
      arc.setFill(Color.WHITE.deriveColor(0.0, 1.0, 1.0, 0.0));
      header.translateYProperty().set(-subHeader.getBoundsInLocal().getHeight() / 4.0);
      subHeader.translateYProperty().set(header.getBoundsInLocal().getHeight() / 4.0 + subHeader.getBoundsInLocal().getHeight() / 2.0);
      logo.getChildren().addAll(root,
          new Circle(radius * 1.1, Colors.WHITE_80),
          arc, header, subHeader
      );
    });
    subHeader.fontProperty().bind(Fonts.LOGO_SMALL.fontProperty(stage::getScene));
    header.fontProperty().bind(Fonts.LOGO.fontProperty(stage::getScene));

    addEventHandler(stage, () ->
            Platform.runLater(() -> {
              stage.setFullScreen(!stage.isFullScreen());
              stage.setResizable(false);
              stage.setResizable(true);
            }),
        KeyCode.CONTROL, KeyCode.SHORTCUT, KeyCode.F);
    addEventHandler(stage, () -> refresh(false), KeyCode.N);
    addEventHandler(stage, () -> refresh(true), KeyCode.S);
    addEventHandler(stage, this::up, KeyCode.UP);
    addEventHandler(stage, this::down, KeyCode.DOWN);
    addEventHandler(stage, this::left, KeyCode.LEFT);
    addEventHandler(stage, this::right, KeyCode.RIGHT);
    addEventHandler(stage, this::escape, KeyCode.ESCAPE);
    addEventHandler(stage, () -> zoom(Double.POSITIVE_INFINITY), KeyCode.EQUALS);
    addEventHandler(stage, () -> zoom(Double.NEGATIVE_INFINITY), KeyCode.MINUS);

    Storage<Stage> stageStorage = OSStageStorage.valueOf(OS.get().name()).newInstance(getClass(), Strings.EMPTY);
    stage.setOnCloseRequest(_ -> stageStorage.save(stage));
    stage.getScene().getWindow().setOnShown(_ ->
        CompletableFuture.delayedExecutor(500, TimeUnit.MILLISECONDS).execute(() ->
            FxUtils.invokeInFx(() -> {
              dividerStorage.update(root);
              stageStorage.update(stage);
            })
        )
    );
    stage.show();
  }

  protected List<FXMLLoader> getFXMLLoader(ResourceBundle resourceBundle) {
    return Collections.singletonList(
        new FXMLLoader(getClass().getResource(String.join(".", "default", "fxml")), resourceBundle)
    );
  }

  @Override
  public void stop() {
    Platform.exit();
  }

  private static void addEventHandler(EventTarget stage, Runnable runnable, KeyCode... codes) {
    stage.addEventHandler(KeyEvent.KEY_RELEASED,
        event -> {
          var combination = String.join("+", Arrays.stream(codes).map(KeyCode::getName).toArray(String[]::new));
          if (KeyCombination.keyCombination(combination).match(event)) {
            runnable.run();
          }
        }
    );
  }
}