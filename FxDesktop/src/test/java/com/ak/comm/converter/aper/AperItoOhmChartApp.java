package com.ak.comm.converter.aper;

import com.ak.comm.converter.ADCVariable;
import com.ak.comm.converter.AbstractSplineCoefficientsChartApp;
import com.ak.numbers.aper.AperCoefficients;

public final class AperItoOhmChartApp extends AbstractSplineCoefficientsChartApp {
  public AperItoOhmChartApp() {
    super(AperCoefficients.I_ADC_TO_OHM, ADCVariable.ADC, AperInVariable.RI1);
  }

  public static void main(String[] args) {
    launch(args);
  }
}
