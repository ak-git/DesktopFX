module com.ak.fx {
  requires com.ak.util;

  requires jsr305;
  requires java.desktop;
  requires java.logging;
  requires spring.context;
  requires spring.beans;
  requires javafx.graphics;
  requires java.prefs;

  exports com.ak.fx.stage;
  exports com.ak.fx.storage;
  exports com.ak.fx.util;
}