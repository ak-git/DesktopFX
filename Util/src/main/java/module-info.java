module com.ak.util {
  requires java.json;
  requires commons.math3;
  requires org.apache.commons.csv;
  requires org.jspecify;
  requires io.jenetics.base;
  requires tech.units.indriya;
  requires org.slf4j;
  requires static org.mockito.junit.jupiter;
  requires static org.mockito;

  exports com.ak.csv;
  exports com.ak.math;
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
}