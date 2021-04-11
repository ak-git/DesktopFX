module com.ak.util {
  requires uom.se;
  requires unit.api;
  requires java.json;
  requires java.logging;
  requires java.sql;

  requires commons.math3;
  requires commons.csv;
  requires jsr305;

  opens com.ak.inverse to org.testng;
  opens com.ak.rsm to org.testng;

  exports com.ak.math to org.testng;

  exports com.ak.util;
  exports com.ak.logging;
  exports com.ak.digitalfilter;
  exports com.ak.numbers;
  exports com.ak.rsm;
}