package com.ak.numbers;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Scanner;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import com.ak.logging.CalibrateBuilders;
import com.ak.util.Extension;
import com.ak.util.LocalIO;

public interface Coefficients extends Supplier<double[]> {
  @Override
  default double[] get() {
    String fileName = getClass().getPackageName().substring(getClass().getPackageName().lastIndexOf(".") + 1);
    InputStream inputStream = getClass().getResourceAsStream(Extension.JSON.attachTo(fileName));
    try {
      LocalIO build = CalibrateBuilders.CALIBRATION.build(fileName);
      Path path = build.getPath();
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
      return CoefficientsUtils.read(new Scanner(readJSON(reader.readObject())));
    }
  }

  default double[][] getPairs() {
    double[] xAndY = get();
    double[][] pairs = new double[xAndY.length / 2][2];

    for (int i = 0; i < pairs.length; i++) {
      pairs[i][0] = xAndY[i * 2];
      pairs[i][1] = xAndY[i * 2 + 1];
    }
    return pairs;
  }

  String readJSON(@Nonnull JsonObject object);
}
