module com.ak.util {
  requires uom.se;
  requires unit.api;
  requires java.json;

  requires commons.math3;
  requires jsr305;
  requires java.logging;
  requires io.jenetics.base;

  opens com.ak.inverse to org.testng;
  opens com.ak.rsm to org.testng;

  exports com.ak.util;
  exports com.ak.logging;
  exports com.ak.digitalfilter;
  exports com.ak.numbers;
}