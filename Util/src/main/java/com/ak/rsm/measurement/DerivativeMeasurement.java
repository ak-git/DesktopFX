package com.ak.rsm.measurement;

import com.ak.rsm.resistance.DerivativeResistivity;
import com.ak.rsm.system.InexactTetrapolarSystem;

public interface DerivativeMeasurement extends Measurement, DerivativeResistivity<InexactTetrapolarSystem> {
}
