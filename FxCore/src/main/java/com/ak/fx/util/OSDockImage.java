package com.ak.fx.util;

import javafx.scene.image.Image;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public enum OSDockImage {
  WINDOWS,
  MAC {
    @Override
    public void setIconImage(Stage stage, URL imageURL) {
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

  public void setIconImage(Stage stage, URL imageURL) {
    stage.getIcons().add(new Image(imageURL.toString()));
  }
}