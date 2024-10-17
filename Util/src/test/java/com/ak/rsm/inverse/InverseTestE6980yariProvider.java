package com.ak.rsm.inverse;

import com.ak.rsm.measurement.TetrapolarDerivativeMeasurement;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * "","TIME","POSITION","R1-begin","R2-begin","R1-end","R2-end"
 * "1",30.75,5.46,110.331476190476,189.490587301587,110.835396825397,190.355706349206
 * "2",31.75,5.25,109.764753968254,188.44803968254,110.260722222222,189.369904761905
 * "3",32.75,5.04,109.265650793651,187.591301587302,109.743396825397,188.461261904762
 * "4",33.75,4.83,108.634753968254,186.571531746032,109.107412698413,187.411444444444
 * "5",34.75,4.62,107.903198412698,185.507555555556,108.407222222222,186.270785714286
 * "6",35.75,4.41,107.233547619048,184.564126984127,107.685031746032,185.255476190476
 * "7",36.75,4.2,106.572126984127,183.665293650794,106.959785714286,184.233182539683
 * "8",37.75,3.99,106.036023809524,182.919547619048,106.424333333333,183.478
 * "9",38.75,3.78,105.454865079365,182.075968253968,105.799944444444,182.591277777778
 */
class InverseTestE6980yariProvider {
  private InverseTestE6980yariProvider() {
  }

  static Stream<Arguments> e12() {
    return Stream.of(
        arguments(
            TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system2(7.0)
                .ofOhms(110.331476190476, 189.490587301587, 110.835396825397, 190.355706349206),
            TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system2(7.0)
                .ofOhms(109.764753968254, 188.44803968254, 110.260722222222, 189.369904761905),
            5.46 - 5.25
        )
    );
  }

  static Stream<Arguments> e13() {
    return Stream.of(
        arguments(
            TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system2(7.0)
                .ofOhms(110.331476190476, 189.490587301587, 110.835396825397, 190.355706349206),
            TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system2(7.0)
                .ofOhms(109.265650793651, 187.591301587302, 109.743396825397, 188.461261904762),
            5.46 - 5.04
        )
    );
  }

  static Stream<Arguments> e14() {
    return Stream.of(
        arguments(
            TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system2(7.0)
                .ofOhms(110.331476190476, 189.490587301587, 110.835396825397, 190.355706349206),
            TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system2(7.0)
                .ofOhms(108.634753968254, 186.571531746032, 109.107412698413, 187.411444444444),
            5.46 - 4.83
        )
    );
  }

  static Stream<Arguments> e15() {
    return Stream.of(
        arguments(
            TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system2(7.0)
                .ofOhms(110.331476190476, 189.490587301587, 110.835396825397, 190.355706349206),
            TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system2(7.0)
                .ofOhms(107.903198412698, 185.507555555556, 108.407222222222, 186.270785714286),
            5.46 - 4.62
        )
    );
  }

  static Stream<Arguments> e16() {
    return Stream.of(
        arguments(
            TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system2(7.0)
                .ofOhms(110.331476190476, 189.490587301587, 110.835396825397, 190.355706349206),
            TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system2(7.0)
                .ofOhms(107.233547619048, 184.564126984127, 107.685031746032, 185.255476190476),
            (5.46 - 4.41) - 0.105 * 4
        )
    );
  }

  static Stream<Arguments> e17() {
    return Stream.of(
        arguments(
            TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system2(7.0)
                .ofOhms(110.331476190476, 189.490587301587, 110.835396825397, 190.355706349206),
            TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system2(7.0)
                .ofOhms(106.572126984127, 183.665293650794, 106.959785714286, 184.233182539683),
            5.46 - 4.2
        )
    );
  }

  static Stream<Arguments> e18() {
    return Stream.of(
        arguments(
            TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system2(7.0)
                .ofOhms(110.331476190476, 189.490587301587, 110.835396825397, 190.355706349206),
            TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system2(7.0)
                .ofOhms(106.036023809524, 182.919547619048, 106.424333333333, 183.478),
            5.46 - 3.99
        )
    );
  }

  static Stream<Arguments> e19() {
    return Stream.of(
        arguments(
            TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system2(7.0)
                .ofOhms(110.331476190476, 189.490587301587, 110.835396825397, 190.355706349206),
            TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system2(7.0)
                .ofOhms(105.454865079365, 182.075968253968, 105.799944444444, 182.591277777778),
            5.46 - 3.78
        )
    );
  }
}
