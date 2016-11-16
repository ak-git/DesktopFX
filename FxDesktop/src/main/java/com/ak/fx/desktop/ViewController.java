package com.ak.fx.desktop;

import java.net.URL;
import java.util.ResourceBundle;

import javax.annotation.Nonnull;

import com.ak.fx.scene.MilliGrid;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

public final class ViewController implements Initializable {
  @Nonnull
  @FXML
  public MilliGrid root = new MilliGrid();

  @Override
  public void initialize(@Nonnull URL location, @Nonnull ResourceBundle resources) {
  }
}
