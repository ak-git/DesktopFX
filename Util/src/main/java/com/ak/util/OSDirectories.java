package com.ak.util;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.ak.util.Strings.EMPTY;

public enum OSDirectories {
  ;

  private static final String USER_HOME_PATH;

  static {
    USER_HOME_PATH = Optional.ofNullable(System.getProperty("user.home")).orElse(EMPTY);
  }

  public static final String VENDOR_ID = Stream.of(OSDirectories.class.getPackage().getName().split("\\.")).limit(2).
      collect(Collectors.joining("."));

  @Nonnull
  public static Path getDirectory(@Nonnull String... candidates) {
    return Arrays.stream(candidates)
        .map(p -> Paths.get(USER_HOME_PATH).resolve(p))
        .filter(p -> Files.isDirectory(p) && Files.isWritable(p) && Files.exists(p))
        .filter(p -> {
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
