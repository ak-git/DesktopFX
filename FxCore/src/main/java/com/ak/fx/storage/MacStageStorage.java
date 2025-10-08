package com.ak.fx.storage;

import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.time.Duration;
import java.time.Instant;

import static com.ak.util.UIConstants.UI_DELAY_3SEC;

final class MacStageStorage extends AbstractStageStorage {
  private Instant fullScreenEventInstant = Instant.now();

  MacStageStorage(Class<?> c, String nodeName) {
    super(c, nodeName);
  }

  @Override
  public void save(Stage stage) {
    if (!Duration.between(fullScreenEventInstant, Instant.now()).minus(UI_DELAY_3SEC).isNegative()) {
      saveFullScreenState(stage.isFullScreen());
    }
    if (stage.isMaximized()) {
      boolean no = Screen.getScreensForRectangle(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight()).
          filtered(screen -> {
            Rectangle2D screenBounds = screen.getVisualBounds();
            return Double.compare(screenBounds.getWidth(), stage.getWidth()) == 0 &&
                Double.compare(screenBounds.getHeight(), stage.getHeight()) == 0;
          }).isEmpty();
      if (no) {
        stage.setMaximized(false);
      }
    }
    super.save(stage);
  }

  @Override
  public void update(Stage stage) {
    stage.fullScreenProperty().addListener((_, oldValue, _) -> {
      fullScreenEventInstant = Instant.now();
      saveFullScreenState(oldValue);
      stage.setResizable(false);
      stage.setResizable(true);
    });
    super.update(stage);
  }
}
