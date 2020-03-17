package com.ak.fx.desktop;

import java.util.Arrays;
import java.util.function.ToDoubleFunction;
import java.util.stream.DoubleStream;

import javax.annotation.Nonnull;

import com.ak.rsm.Resistance3Layer;
import com.ak.rsm.TetrapolarSystem;
import com.ak.util.LineFileBuilder;
import com.ak.util.Metrics;
import com.ak.util.Strings;
import org.beryx.textio.TextIO;
import org.beryx.textio.system.SystemTextTerminal;

import static tec.uom.se.unit.MetricPrefix.MILLI;
import static tec.uom.se.unit.Units.METRE;

public final class R3Output {
  private static final String REGEX = "(.*:.*:.*)|(.* .* .*)";

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
      double[] rho1A = parseSequence(textIO.newStringInputReader().withDefaultValue("10:70:10").withPattern(REGEX).read(Strings.rho(1)));
      double[] rho2A = parseSequence(textIO.newStringInputReader().withDefaultValue("2.6 2.7 2.8").withPattern(REGEX).read(Strings.rho(2)));
      double[] rho3A = parseSequence(textIO.newStringInputReader().withDefaultValue("100:100:1").withPattern(REGEX).read(Strings.rho(3)));
      double[] h1A = parseSequence(textIO.newStringInputReader().withDefaultValue("0:20:0.1").withPattern(REGEX).read(String.format("h%s, mm", Strings.low(1))));
      double[] h2mh1A = parseSequence(textIO.newStringInputReader().withDefaultValue("0:50:0.1").withPattern(REGEX).read(String.format("h%s - h%s, mm", Strings.low(2), Strings.low(1))));

      terminal.println();
      terminal.println("################");
      terminal.println("# Check parameters #");
      terminal.println("################");
      terminal.println();
      terminal.println(String.format("L / s = %.1f", lToS));
      terminal.println(String.format("s, mm = %s", Strings.toString("%.1f", smmA)));
      terminal.println(String.format("%s = %s", Strings.rho(1), Strings.toString("%.1f", rho1A)));
      terminal.println(String.format("%s = %s", Strings.rho(2), Strings.toString("%.1f", rho2A)));
      terminal.println(String.format("%s = %s", Strings.rho(3), Strings.toString("%.1f", rho3A)));
      terminal.println(String.format("%s = %s", String.format("h%s, mm", Strings.low(1)), Strings.toString("%.1f", h1A)));
      terminal.println(String.format("%s = %s", String.format("h%s - h%s, mm", Strings.low(2), Strings.low(1)), Strings.toString("%.1f", h2mh1A)));

      if (textIO.newBooleanInputReader().withDefaultValue(true).read("Run?")) {
        for (double smm : smmA) {
          TetrapolarSystem system = new TetrapolarSystem(smm, smm * lToS, MILLI(METRE));
          for (double rho1 : rho1A) {
            for (double rho2 : rho2A) {
              for (double rho3 : rho3A) {
                LineFileBuilder.of("%.1f %.1f %.6f")
                    .xStream(() -> Arrays.stream(h1A))
                    .yStream(() -> Arrays.stream(h2mh1A))
                    .generate(String.format("s = %.1f mm; rho1 = %.1f; rho2 = %.1f; rho3 = %.1f.txt", smm, rho1, rho2, rho3),
                        (h1, h2mh1) -> {
                          int p1 = (int) h1 * 10;
                          int p2mp1 = (int) h2mh1 * 10;
                          terminal.println(String.format("Calculating s = %.1f mm; rho1 = %.1f; rho2 = %.1f; rho3 = %.1f.txt", smm, rho1, rho2, rho3));
                          return new Resistance3Layer(system, Metrics.fromMilli(0.1)).value(rho1, rho2, rho3, p1, p2mp1);
                        });
              }
            }
          }

          if (textIO.newBooleanInputReader().withDefaultValue(true).read("Run again?")) {
            terminal.resetToBookmark("MAIN");
          }
          else {
            break;
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
      return DoubleStream.iterate(firstSecondStep[0], x -> x <= firstSecondStep[1], x -> x + firstSecondStep[2]).toArray();
    }
    else if (s.contains(Strings.SPACE)) {
      return Arrays.stream(s.split(Strings.SPACE)).mapToDouble(stringToDoubleFunction).toArray();
    }
    else {
      throw new RuntimeException(s);
    }
  }
}
