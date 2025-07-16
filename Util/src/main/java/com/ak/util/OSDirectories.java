package com.ak.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.ak.util.Strings.EMPTY;

public enum OSDirectories {
  ;

  private static final String USER_HOME_PATH;

  static {
    USER_HOME_PATH = Optional.ofNullable(System.getProperty("user.home")).orElse(EMPTY);
  }

  public static final String VENDOR_ID = OSDirectories.class.getPackage().getName().split("\\.")[1];

  public static Path getDirectory(String... candidates) {
    return Arrays.stream(Objects.requireNonNull(candidates))
        .map(p -> Paths.get(USER_HOME_PATH).resolve(p))
        .filter(p -> {
          if (Files.isDirectory(p) && Files.isWritable(p) && Files.exists(p)) {
            return true;
          }
          try {
            Files.createDirectories(p);
            Path testDirectory = p.resolve("permissions_check");
            Files.createDirectories(testDirectory);
            Files.delete(testDirectory);
            return true;
          }
          catch (IOException e) {
            Logger.getLogger(OSDirectories.class.getName()).log(Level.WARNING, e,
                () ->
                    """
                        Unable to access directory [%s].
                        Please make sure that you have the appropriate permissions and no Antivirus program is blocking the access.
                        In case you use cloud storage, verify that your cloud storage is working and you are logged in."""
                        .formatted(p)
            );
            return false;
          }
        })
        .findFirst()
        .orElse(Paths.get(EMPTY));
  }
}
