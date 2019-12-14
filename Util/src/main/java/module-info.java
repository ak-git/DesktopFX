module com.ak.util {
  requires uom.se;
  requires unit.api;
  requires java.json;

  requires commons.math3;
  requires jsr305;
  requires java.logging;
  requires java.desktop;

  opens com.ak.inverse to org.testng;
  opens com.ak.digitalfilter to org.testng;
  opens com.ak.util to org.testng;
  opens com.ak.math to org.testng;
  opens com.ak.rsm to org.testng;
  opens com.ak.logging to org.testng;
  opens com.ak.numbers to org.testng;
  opens com.ak.storage to org.testng;

  exports com.ak.util;
  exports com.ak.logging;
  exports com.ak.digitalfilter;
  exports com.ak.storage;
  exports com.ak.numbers;
}