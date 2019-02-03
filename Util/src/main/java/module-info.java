module com.ak.util {
  requires uom.se;
  requires java.json;

  requires commons.math3;
  requires jsr305;
  requires java.logging;
  requires java.desktop;

  exports com.ak.util;
  exports com.ak.logging;
  exports com.ak.digitalfilter;
}