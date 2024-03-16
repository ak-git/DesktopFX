package com.ak.appliance.aper.comm.converter;

import com.ak.appliance.aper.numbers.AperCoefficients;
import com.ak.appliance.app.comm.converter.AbstractSplineCoefficientsChartApp;
import com.ak.comm.converter.ADCVariable;

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
