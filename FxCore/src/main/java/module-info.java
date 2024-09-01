module com.ak.fx {
  requires com.ak.util;

  requires java.desktop;
  requires java.logging;
  requires javafx.graphics;
  requires javafx.controls;
  requires java.prefs;
  requires jsr305;
  requires org.jspecify;
  requires com.ak.comm;

  exports com.ak.appliance.app.comm.converter;
  exports com.ak.fx.stage;
  exports com.ak.fx.storage;
  exports com.ak.fx.util;
}