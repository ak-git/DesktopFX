package com.ak.app.rcm.comm.converter;

import com.ak.app.comm.converter.AbstractSplineCoefficientsChartApp;
import com.ak.appliance.rcm.comm.converter.RcmOutVariable;
import com.ak.appliance.rcm.numbers.RcmCoefficients;
import com.ak.comm.converter.ADCVariable;

/**
 * x = ADC, y = R(I-I)
 */
public final class RcmItoOhmChartApp extends AbstractSplineCoefficientsChartApp<ADCVariable, RcmOutVariable> {
  public RcmItoOhmChartApp() {
    super(RcmCoefficients.CC_ADC_TO_OHM.of(1), ADCVariable.ADC, RcmOutVariable.QS_1);
  }

  public static void main(String[] args) {
    launch();
  }
}
