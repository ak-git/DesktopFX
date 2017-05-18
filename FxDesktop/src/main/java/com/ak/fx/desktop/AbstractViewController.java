package com.ak.fx.desktop;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import com.ak.comm.GroupService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;

public abstract class AbstractViewController implements Initializable {
  @Nonnull
  private final GroupService<?, ?, ?> service;
  @Nonnull
  @FXML
  private Pane root = new Pane();

  @Inject
  public AbstractViewController(@Nonnull GroupService<?, ?, ?> service) {
    this.service = service;
  }

  @Override
  public final void initialize(@Nonnull URL location, @Nonnull ResourceBundle resources) {
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

  protected final Pane root() {
    return root;
  }
}
