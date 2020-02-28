package com.ak.comm.converter.aper;

import com.ak.comm.converter.ADCVariable;
import com.ak.numbers.aper.AperCoefficients;

/**
 * x = ADC, y = R(I-I)
 */
public final class AperItoOhmChartApp extends AbstractSplineCoefficientsChartApp<ADCVariable, AperOutVariable> {
  public AperItoOhmChartApp() {
    super(AperCoefficients.ADC_TO_OHM, ADCVariable.ADC, AperOutVariable.CCR1);
  }

  public static void main(String[] args) {
    launch();
  }
}