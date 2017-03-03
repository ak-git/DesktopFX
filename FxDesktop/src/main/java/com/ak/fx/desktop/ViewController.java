package com.ak.fx.desktop;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import com.ak.comm.GroupService;
import com.ak.fx.scene.MilliGrid;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

public final class ViewController implements Initializable {
  @Nullable
  @FXML
  public MilliGrid root;
  @Nonnull
  private final GroupService<?, ?, ?> service;

  @Inject
  public ViewController(@Nonnull GroupService<?, ?, ?> service) {
    this.service = service;
    service.subscribe(values -> Logger.getLogger(getClass().getName()).log(Level.INFO, Arrays.toString(values)));
  }

  @Override
  public void initialize(@Nonnull URL location, @Nonnull ResourceBundle resources) {
    if (root != null) {
      root.setOnDragOver(event -> {
        Dragboard db = event.getDragboard();
        if (db.hasFiles()) {
          event.acceptTransferModes(TransferMode.COPY);
        }
        else {
          event.consume();
        }
      });
      root.setOnDragDropped(event -> {
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
    }
  }
}
