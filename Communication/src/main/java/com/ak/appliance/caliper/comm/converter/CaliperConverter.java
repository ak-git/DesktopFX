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
    int[] value = new int[2];
    if (sep > 0) {
      try {
        value[0] = Integer.parseInt(frame.substring(0, sep).strip());
        value[1] = Integer.parseInt(frame.substring(sep).strip());
      }
      catch (NumberFormatException e) {
        logger.log(LogUtils.LOG_LEVEL_ERRORS, e, () -> frame);
      }
    }
    return Stream.of(value);
  }
}
