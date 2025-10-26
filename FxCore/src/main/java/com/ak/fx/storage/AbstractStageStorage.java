package com.ak.fx.storage;

import com.ak.fx.util.FxUtils;
import com.ak.util.UIConstants;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.awt.geom.Rectangle2D;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.prefs.BackingStoreException;

abstract class AbstractStageStorage extends AbstractStorage<Stage> {
  private static final String FULL_SCREEN = "fullScreen";
  private static final String MAXIMIZED = "maximized";
  private final Storage<Rectangle2D.Double> boundsStorage;

  AbstractStageStorage(Class<?> c, String nodeName) {
    super(c, nodeName);
    boundsStorage = new BoundsStorage(getClass(), nodeName);
  }

  @Override
  public void save(Stage stage) {
    if (!preferences().getBoolean(FULL_SCREEN, false) &&
        !preferences().getBoolean(MAXIMIZED, false)) {
      boundsStorage.save(new Rectangle2D.Double(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight()));
    }
  }

  @Override
  public void update(Stage stage) {
    stage.maximizedProperty().addListener((_, _, newValue) -> preferences().putBoolean(MAXIMIZED, newValue));
    boundsStorage.get().ifPresentOrElse(
        r -> {
          if (Screen.getPrimary().getVisualBounds().contains(r.getX(), r.getY(), r.getWidth(), r.getHeight())) {
            stage.setX(r.getX());
            stage.setY(r.getY());
            stage.setWidth(r.getWidth());
            stage.setHeight(r.getHeight());
          }
          else {
            centerOnScreen(stage);
          }
        },
        () -> {
        }
    );
    stage.setMaximized(preferences().getBoolean(MAXIMIZED, false));
    CompletableFuture.delayedExecutor(UIConstants.UI_DELAY_750MILLIS.toMillis(), TimeUnit.MILLISECONDS).execute(() ->
        FxUtils.invokeInFx(() -> stage.setFullScreen(preferences().getBoolean(FULL_SCREEN, false)))
    );
  }

  @Override
  public final Optional<Stage> get() {
    throw new UnsupportedOperationException();
  }

  @Override
  public final void delete() throws BackingStoreException {
    super.delete();
    boundsStorage.delete();
  }

  final void saveFullScreenState(boolean state) {
    preferences().putBoolean(FULL_SCREEN, state);
  }

  private static void centerOnScreen(Stage stage) {
    stage.setWidth(Screen.getPrimary().getVisualBounds().getWidth());
    stage.setHeight(Screen.getPrimary().getVisualBounds().getHeight());
    stage.centerOnScreen();
  }
}
