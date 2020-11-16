package com.ak.comm.converter.aper;

import com.ak.comm.converter.ADCVariable;
import com.ak.comm.converter.app.AbstractSplineCoefficientsChartApp;
import com.ak.numbers.aper.AperCoefficients;

/**
 * x = ADC, y = R(I-I)
 */
public final class AperItoOhmChartApp extends AbstractSplineCoefficientsChartApp<ADCVariable, AperStage2UnitsVariable> {
  public AperItoOhmChartApp() {
    super(AperCoefficients.ADC_TO_OHM, ADCVariable.ADC, AperStage2UnitsVariable.CCR1);
  }

  public static void main(String[] args) {
    launch();
  }
}
