package com.ak.comm.converter.rcm;

import com.ak.comm.converter.ADCVariable;
import com.ak.comm.converter.app.AbstractSplineCoefficientsChartApp;
import com.ak.numbers.rcm.RcmCoefficients;

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
