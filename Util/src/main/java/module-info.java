module com.ak.util {
  requires uom.se;
  requires unit.api;
  requires java.json;
  requires java.logging;
  requires java.sql;

  requires commons.math3;
  requires commons.csv;
  requires jsr305;
  requires io.jenetics.base;


  exports com.ak.math to org.testng;

  exports com.ak.util;
  exports com.ak.logging;
  exports com.ak.digitalfilter;
  exports com.ak.numbers;
  exports com.ak.rsm.resistance;
  exports com.ak.rsm.system;
  exports com.ak.rsm.measurement;
  exports com.ak.rsm.prediction;
  exports com.ak.rsm.relative;
  exports com.ak.rsm.apparent;

  opens com.ak.inverse to org.testng;
  opens com.ak.rsm.resistance to org.testng;
  opens com.ak.rsm.system to org.testng;
  opens com.ak.rsm.measurement to org.testng;
  opens com.ak.rsm.relative to org.testng;
  opens com.ak.rsm.apparent to org.testng;
}