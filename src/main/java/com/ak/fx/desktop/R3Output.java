package com.ak.fx.desktop;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.function.ToDoubleFunction;
import java.util.stream.DoubleStream;

import javax.annotation.Nonnull;

import com.ak.rsm.Resistance3Layer;
import com.ak.rsm.TetrapolarSystem;
import com.ak.util.Extension;
import com.ak.util.LineFileCollector;
import com.ak.util.Metrics;
import com.ak.util.Strings;
import org.beryx.textio.TextIO;
import org.beryx.textio.system.SystemTextTerminal;

import static tec.uom.se.unit.MetricPrefix.MILLI;
import static tec.uom.se.unit.Units.METRE;

public final class R3Output {
  private static final String REGEX = "(.*:.*:.*)|(.* .* .*)|(\\S*)";

  private R3Output() {
  }

  public static void main(String[] args) {
    SystemTextTerminal terminal = new SystemTextTerminal();
    TextIO textIO = new TextIO(terminal);
    terminal.println("################");
    terminal.println("# ak: Resistance 3 Layer #");
    terminal.println("################");
    terminal.println();
    terminal.setBookmark("MAIN");
    while (!Thread.currentThread().isInterrupted()) {
      double lToS = textIO.newDoubleInputReader().withDefaultValue(3.0).withMinVal(0.1).read("L / s");
      double[] smmA = parseSequence(textIO.newStringInputReader().withDefaultValue("1:30:1").withPattern(REGEX).read("s, mm"));
      double[] rho1A = parseSequence(textIO.newStringInputReader().withDefaultValue("10:70:10").withPattern(REGEX).read("rho1"));
      double[] rho2A = parseSequence(textIO.newStringInputReader().withDefaultValue("2.6 2.7 2.8").withPattern(REGEX).read("rho2"));
      double[] rho3A = parseSequence(textIO.newStringInputReader().withDefaultValue("100:100:1").withPattern(REGEX).read("rho3"));
      double[] h1A = parseSequence(textIO.newStringInputReader().withDefaultValue("0:20:0.1").withPattern(REGEX).read("h1, mm"));
      double[] h2mh1A = parseSequence(textIO.newStringInputReader().withDefaultValue("0:50:0.1").withPattern(REGEX).read("h2, mm"));

      terminal.println();
      terminal.println("################");
      terminal.println("# Check parameters #");
      terminal.println("################");
      terminal.println();
      terminal.println(String.format("L / s = %.1f", lToS));
      terminal.println(String.format("s = %s mm", Strings.toString("%.1f", smmA)));
      terminal.println(String.format("rho1 = %s Ohm-m", Strings.toString("%.1f", rho1A)));
      terminal.println(String.format("rho2 = %s Ohm-m", Strings.toString("%.1f", rho2A)));
      terminal.println(String.format("rho3 = %s Ohm-m", Strings.toString("%.1f", rho3A)));
      terminal.println(String.format("h1 = %s mm", Strings.toString("%.1f", h1A)));
      terminal.println(String.format("h2 = %s mm", Strings.toString("%.1f", h2mh1A)));
      terminal.println();

      if (textIO.newBooleanInputReader().withDefaultValue(true).read("Run?")) {
        for (double smm : smmA) {
          TetrapolarSystem system = new TetrapolarSystem(smm, smm * lToS, MILLI(METRE));
          try (LineFileCollector collector = new LineFileCollector(Paths.get(Extension.TXT.attachTo("out")), LineFileCollector.Direction.VERTICAL)) {
            collector.accept("s, mm\tL, mm\trho1, Ohm-m\trho2, Ohm-m\trho3, Ohm-m\th1, mm\th2mm");
            for (double rho1 : rho1A) {
              for (double rho2 : rho2A) {
                for (double rho3 : rho3A) {
                  terminal.println(String.format("Calculating s = %.1f mm; rho1 = %.1f; rho2 = %.1f; rho3 = %.1f.txt", smm, rho1, rho2, rho3));

                  for (double h1 : h1A) {
                    for (double h2mh1 : h2mh1A) {
                      int p1 = (int) h1 * 10;
                      int p2mp1 = (int) h2mh1 * 10;
                      double result = new Resistance3Layer(system, Metrics.fromMilli(0.1)).value(rho1, rho2, rho3, p1, p2mp1);
                      collector.accept(
                          String.format("%.1f\t%.1f\t%.2f\t%.2f\t%.2f\t%.1f\t%.1f\t%.12f",
                              smm, smm * lToS, rho1, rho2, rho3, h1, h2mh1, result)
                      );
                    }
                  }
                }
              }
            }
          }
          catch (IOException e) {
            System.err.println(e);
          }
        }
      }
      else {
        terminal.resetToBookmark("MAIN");
      }
    }
    textIO.dispose();
  }

  @Nonnull
  private static double[] parseSequence(@Nonnull String s) {
    ToDoubleFunction<String> stringToDoubleFunction = str -> {
      try {
        return Double.parseDouble(str.strip());
      }
      catch (Exception ex) {
        return 0.0;
      }
    };

    if (s.contains(":")) {
      double[] firstSecondStep = Arrays.stream(s.split(":")).mapToDouble(stringToDoubleFunction).toArray();
      return DoubleStream.iterate(firstSecondStep[0], x -> x < firstSecondStep[1] + firstSecondStep[2], x -> x + firstSecondStep[2]).toArray();
    }
    else if (s.contains(Strings.SPACE)) {
      return Arrays.stream(s.split(Strings.SPACE)).mapToDouble(stringToDoubleFunction).toArray();
    }
    else {
      return new double[] {stringToDoubleFunction.applyAsDouble(s)};
    }
  }
}
