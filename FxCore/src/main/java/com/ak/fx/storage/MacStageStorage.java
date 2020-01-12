package com.ak.fx.storage;

import java.time.Duration;
import java.time.Instant;

import javax.annotation.Nonnull;

import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

import static com.ak.util.UIConstants.UI_DELAY;

final class MacStageStorage extends AbstractStageStorage {
  @Nonnull
  private Instant fullScreenEventInstant = Instant.now();

  MacStageStorage(@Nonnull Class<?> c) {
    super(c);
  }

  @Override
  public void save(@Nonnull Stage stage) {
    if (!Duration.between(fullScreenEventInstant, Instant.now()).minus(UI_DELAY).isNegative()) {
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
  public void update(@Nonnull Stage stage) {
    stage.fullScreenProperty().addListener((observable, oldValue, newValue) -> {
      fullScreenEventInstant = Instant.now();
      saveFullScreenState(oldValue);
      stage.setResizable(false);
      stage.setResizable(true);
    });
    super.update(stage);
  }
}
