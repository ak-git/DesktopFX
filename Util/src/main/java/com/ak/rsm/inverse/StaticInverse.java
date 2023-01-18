package com.ak.rsm.inverse;

import java.util.Collection;
import java.util.function.UnaryOperator;

import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.rsm.resistance.Resistivity;
import org.apache.commons.math3.complex.Complex;

import static java.lang.StrictMath.log;

final class StaticInverse extends AbstractInverseFunction<Resistivity> {
  @ParametersAreNonnullByDefault
  StaticInverse(Collection<? extends Resistivity> r, UnaryOperator<double[]> subtract) {
    super(r, d -> new Complex(log(d.resistivity())), subtract, Layer2StaticInverse::new);
  }
}
