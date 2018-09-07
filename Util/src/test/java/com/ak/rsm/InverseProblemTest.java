package com.ak.rsm;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import com.ak.inverse.Inequality;
import com.ak.util.LineFileCollector;
import com.ak.util.Strings;
import org.apache.commons.math3.analysis.TrivariateFunction;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.NelderMeadSimplex;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizer;
import org.testng.Assert;
import org.testng.annotations.Test;

import static tec.uom.se.unit.MetricPrefix.MILLI;
import static tec.uom.se.unit.Units.METRE;

public class InverseProblemTest {
  private InverseProblemTest() {
  }

  @Test(enabled = false)
  public static void solveFiles() throws IOException {
    String filteredPrefix = "h = ";
    TrivariateFunction resistance1 = new ResistanceTwoLayer(new TetrapolarSystem(30.0, 90.0, MILLI(METRE)));
    TrivariateFunction resistance2 = new ResistanceTwoLayer(new TetrapolarSystem(30.0, 60.0, MILLI(METRE)));

    try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(Strings.EMPTY), "*.txt")) {
      directoryStream.forEach(path -> {
        if (!path.toString().startsWith(filteredPrefix)) {
          double[] rhos = {12.0, 3.5};
          DoubleStream.iterate(0.010, operand -> operand + 0.0005).limit(10).forEach(hSI -> {
            try (LineFileCollector collector = new LineFileCollector(
                Paths.get(String.format("%s %.1f mm %s", filteredPrefix, hSI * 1000.0, path.getFileName().toString())), LineFileCollector.Direction.VERTICAL);
                 Stream<String> lines = Files.lines(path)) {

              collector.accept(String.join(Strings.TAB, "time", "rho1", "rho2"));
              collector.accept(Strings.SPACE);

              Assert.assertNull(lines.filter(s -> s.matches("\\d+.*")).map(value -> {
                double[] doubles = Arrays.stream(value.split("\\t")).mapToDouble(s -> {
                  try {
                    return NumberFormat.getNumberInstance().parse(s).doubleValue();
                  }
                  catch (ParseException e) {
                    Logger.getLogger(InverseProblemTest.class.getName()).log(Level.INFO, e.getMessage(), e);
                    return 0.0;
                  }
                }).toArray();

                double time = doubles[0];
                double r1 = doubles[2] - doubles[1] / 1000.0;
                double r2 = doubles[5] - doubles[4] / 1000.0;

                SimplexOptimizer optimizer = new SimplexOptimizer(-1, 1.0e-14);
                PointValuePair optimum = optimizer.optimize(new MaxEval(30000), new ObjectiveFunction(point -> {
                      Inequality inequality = Inequality.logDifference();
                      inequality.applyAsDouble(resistance1.value(point[0], point[1], hSI), r1);
                      inequality.applyAsDouble(resistance2.value(point[0], point[1], hSI), r2);
                      return inequality.getAsDouble();
                    }),
                    GoalType.MINIMIZE, new NelderMeadSimplex(2, 0.001), new InitialGuess(rhos)
                );

                for (int i = 0; i < optimum.getPoint().length; i++) {
                  rhos[i] = optimum.getPoint()[i];
                }

                return Stream.concat(
                    Stream.of(String.format("%.03f", time)),
                    Arrays.stream(optimum.getPoint()).mapToObj(d -> String.format("%.06f", d))
                ).collect(Collectors.joining(Strings.TAB));
              }).collect(collector));
            }
            catch (IOException e) {
              Logger.getLogger(InverseProblemTest.class.getName()).log(Level.INFO, e.getMessage(), e);
            }
          });
        }
      });
    }
  }
}