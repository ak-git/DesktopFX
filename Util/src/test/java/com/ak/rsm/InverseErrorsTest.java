package com.ak.rsm;

import java.util.Arrays;

import com.ak.util.CSVLineFileBuilder;
import org.testng.annotations.Test;

public class InverseErrorsTest {
  @Test(enabled = false)
  public void testCSV() {
    CSVLineFileBuilder
        .of((k, hToL) -> InverseDynamic.INSTANCE.errors(
            Arrays.asList(TetrapolarSystem.systems2(0.0001, 10.0)),
            new Layer2RelativeMedium(k, hToL)
        ))
        .xRange(-1.0, 1.0, 0.2)
        .yLog10Range(0.01, 1.0)
        .saveTo("k2", layers -> "%.4f".formatted(Math.abs(layers.k12AbsError() / layers.k12()) / (0.0001 / 50.0)))
        .generate();
  }
}
