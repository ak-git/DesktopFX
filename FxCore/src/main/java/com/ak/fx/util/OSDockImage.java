package com.ak.fx.util;

import java.awt.Taskbar;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;

import javafx.scene.image.Image;
import javafx.stage.Stage;

public enum OSDockImage {
  WINDOWS,
  MAC {
    @Override
    public void setIconImage(@Nonnull Stage stage, @Nonnull URL imageURL) {
      super.setIconImage(stage, imageURL);
      try {
        Taskbar.getTaskbar().setIconImage(ImageIO.read(imageURL));
      }
      catch (IOException e) {
        Logger.getLogger(getClass().getName()).log(Level.CONFIG, e.getMessage(), e);
      }
    }
  },
  UNIX;

  public void setIconImage(@Nonnull Stage stage, @Nonnull URL imageURL) {
    stage.getIcons().add(new Image(imageURL.toString()));
  }
}