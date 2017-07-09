package com.ak.fx.desktop;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;

import com.ak.comm.GroupService;
import com.ak.comm.converter.Variable;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

public abstract class AbstractViewController<RESPONSE, REQUEST, EV extends Enum<EV> & Variable<EV>> implements Initializable {
  @Nonnull
  private final GroupService<RESPONSE, REQUEST, EV> service;
  @Nonnull
  @FXML
  private Region root = new Pane();

  public AbstractViewController(@Nonnull GroupService<RESPONSE, REQUEST, EV> service) {
    this.service = service;
  }

  @OverridingMethodsMustInvokeSuper
  @Override
  public void initialize(@Nonnull URL location, @Nonnull ResourceBundle resources) {
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
    root.getScene().addEventHandler(KeyEvent.KEY_RELEASED, event -> {
      if (KeyCombination.keyCombination("Shortcut+N").match(event)) {
        service.refresh();
      }
    });
  }

  protected final GroupService<RESPONSE, REQUEST, EV> service() {
    return service;
  }
}
