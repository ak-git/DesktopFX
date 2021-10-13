package com.ak.logging;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.NoSuchAlgorithmException;
import java.util.stream.Stream;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class CalibrateBuildersTest {
  @DataProvider(name = "builders")
  public static Object[][] builders() {
    return Stream.of(CalibrateBuilders.values()).
        map(builder -> {
          try {
            return new Object[] {builder.build(OutputBuildersTest.randomFileName()).getPath()};
          }
          catch (IOException | NoSuchAlgorithmException e) {
            return new Object[] {null};
          }
        }).toArray(Object[][]::new);
  }

  @Test(dataProvider = "builders")
  public void testBuild(Path path) throws IOException {
    Assert.assertNotNull(path);
    WritableByteChannel channel = Files.newByteChannel(path,
        StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
    channel.write(ByteBuffer.wrap(LogBuildersTest.class.getName().getBytes(Charset.defaultCharset())));
    channel.close();
    CalibrateBuilders.CALIBRATION.clean();
    Assert.assertTrue(Files.notExists(path));
  }
}