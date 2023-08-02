package com.ak.numbers;

import com.ak.logging.CalibrateBuilders;
import com.ak.util.Extension;
import com.ak.util.LocalIO;

import javax.annotation.Nonnull;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public interface Coefficients extends Supplier<double[]> {
  @Override
  default double[] get() {
    var fileName = getClass().getPackageName().substring(getClass().getPackageName().lastIndexOf(".") + 1);
    var inputStream = Objects.requireNonNull(getClass().getResourceAsStream(Extension.JSON.attachTo(fileName)));
    try {
      LocalIO build = CalibrateBuilders.build(fileName);
      var path = build.getPath();
      if (Files.notExists(path, LinkOption.NOFOLLOW_LINKS)) {
        Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
        inputStream.close();
      }
      inputStream = build.openInputStream();
    }
    catch (IOException e) {
      Logger.getLogger(Coefficients.class.getName()).log(Level.WARNING, fileName, e);
    }
    try (JsonReader reader = Json.createReader(inputStream)) {
      return read(new Scanner(readJSON(reader.readObject())));
    }
  }

  default double[][] getPairs() {
    double[] xAndY = get();
    var pairs = new double[xAndY.length / 2][2];

    for (var i = 0; i < pairs.length; i++) {
      pairs[i][0] = xAndY[i * 2];
      pairs[i][1] = xAndY[i * 2 + 1];
    }
    return pairs;
  }

  String readJSON(@Nonnull JsonObject object);

  static double[] read(@Nonnull Scanner scanner) {
    scanner.useLocale(Locale.ROOT);
    Collection<Double> coeffs = new LinkedList<>();
    while (scanner.hasNext() && !scanner.hasNextDouble()) {
      scanner.next();
    }
    while (scanner.hasNextDouble()) {
      coeffs.add(scanner.nextDouble());
    }
    return coeffs.stream().mapToDouble(Double::doubleValue).toArray();
  }
}
