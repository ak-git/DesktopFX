package com.ak.eye;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public enum PointLoader {
  INSTANCE;

  private final List<Point> points;

  PointLoader() {
    List<Point> points = Collections.emptyList();
    Path fileToLoad = Paths.get("points.txt");
    try {
      points = Files.lines(fileToLoad).map(s -> {
        String[] xAndY = s.trim().split("\\s+");
        return new Point(Double.parseDouble(xAndY[0]), Double.parseDouble(xAndY[1]));
      }).collect(Collectors.toList());
    }
    catch (IOException ex) {
      Logger.getLogger(getClass().getName()).log(Level.SEVERE, fileToLoad.toAbsolutePath().toString(), ex);
    }
    this.points = Collections.unmodifiableList(points);
  }

  public List<Point> getPoints() {
    return points;
  }
}
