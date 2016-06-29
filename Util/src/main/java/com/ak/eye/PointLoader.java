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

import javax.annotation.Nonnull;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

@ThreadSafe
@Immutable
public enum PointLoader {
  INSTANCE;

  @Nonnull
  @GuardedBy("this")
  private final List<Vector2D> points;

  PointLoader() {
    List<Vector2D> points = Collections.emptyList();
    Path fileToLoad = Paths.get("points.txt");
    try {
      points = Files.lines(fileToLoad).map(s -> {
        String[] xAndY = s.trim().split("\\s+");
        return new Vector2D(Double.parseDouble(xAndY[0]), Double.parseDouble(xAndY[1]));
      }).collect(Collectors.toList());
    }
    catch (IOException ex) {
      Logger.getLogger(getClass().getName()).log(Level.SEVERE, fileToLoad.toAbsolutePath().toString(), ex);
    }
    this.points = Collections.unmodifiableList(points);
  }

  public List<Vector2D> getPoints() {
    return points;
  }
}
