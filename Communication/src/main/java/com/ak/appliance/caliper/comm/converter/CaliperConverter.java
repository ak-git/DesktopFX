package com.ak.appliance.caliper.comm.converter;

import com.ak.comm.bytes.LogUtils;
import com.ak.comm.converter.AbstractConverter;
import com.ak.util.Strings;

import java.util.logging.Logger;
import java.util.stream.Stream;

import static com.ak.appliance.caliper.comm.converter.CaliperVariable.FREQUENCY;

public final class CaliperConverter extends AbstractConverter<String, CaliperVariable> {
  private final Logger logger = Logger.getLogger(getClass().getName());

  public CaliperConverter() {
    super(CaliperVariable.class, FREQUENCY);
  }

  @Override
  protected Stream<int[]> innerApply(String frame) {
    int sep = frame.indexOf(Strings.TAB);
    int value = 0;
    if (sep > 0) {
      try {
        value = Integer.parseInt(frame.substring(0, sep));
      }
      catch (NumberFormatException e) {
        logger.log(LogUtils.LOG_LEVEL_ERRORS, e, () -> frame);
      }
    }
    return Stream.of(new int[] {value});
  }
}
