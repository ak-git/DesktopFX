package com.ak.digitalfilter;

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
import java.util.stream.Stream;

import com.ak.util.LineFileCollector;
import com.ak.util.Strings;
import org.testng.annotations.Test;

public class FiltersTest {
  @Test(enabled = false)
  public void textFiles() throws IOException {
    String filteredPrefix = "Filtered - ";
    int column = 0;

    try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(Strings.EMPTY), "*.txt")) {
      directoryStream.forEach(path -> {
        if (!path.toString().startsWith(filteredPrefix)) {
          DigitalFilter filter = FilterBuilder.of().smoothingImpulsive(10).buildNoDelay();

          try (LineFileCollector collector = new LineFileCollector(
              Paths.get(String.join(Strings.EMPTY, filteredPrefix, path.getFileName().toString())), LineFileCollector.Direction.VERTICAL)) {
            filter.forEach(values ->
                collector.accept(Arrays.stream(values).mapToObj(String::valueOf).collect(Collectors.joining(Strings.TAB))));

            try (Stream<String> lines = Files.lines(path)) {
              lines.filter(s -> s.matches("\\d+.*")).mapToInt(value -> {
                try {
                  return NumberFormat.getIntegerInstance().parse(value.split("\\t")[column]).intValue();
                }
                catch (ParseException e) {
                  Logger.getLogger(FiltersTest.class.getName()).log(Level.INFO, e.getMessage(), e);
                  return 0;
                }
              }).forEach(filter::accept);
            }
            catch (IOException e) {
              Logger.getLogger(FiltersTest.class.getName()).log(Level.INFO, e.getMessage(), e);
            }
          }
          catch (IOException e) {
            Logger.getLogger(FiltersTest.class.getName()).log(Level.INFO, e.getMessage(), e);
          }
        }
      });
    }
  }
}