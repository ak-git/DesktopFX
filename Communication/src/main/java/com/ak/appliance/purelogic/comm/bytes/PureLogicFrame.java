package com.ak.appliance.purelogic.comm.bytes;

import com.ak.comm.bytes.BufferFrame;

import javax.measure.MetricPrefix;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.logging.Logger;

import static com.ak.comm.bytes.LogUtils.LOG_LEVEL_ERRORS;
import static com.ak.util.Strings.NEW_LINE;
import static com.ak.util.Strings.SPACE;
import static tech.units.indriya.unit.Units.METRE;

public final class PureLogicFrame extends BufferFrame {
  public static final PureLogicFrame ALIVE = new PureLogicFrame(0);
  public static final int FRAME_LEN = 15;
  private static final String ALIVE_COMMAND = "PLC001-G2";
  private static final String STEP_COMMAND = "STEP";
  private final int microns;

  /**
   * 1 big step == 16 steps
   * 1 big step == 1.8 degree
   * 200 big steps == 360 degrees == 3 mm
   * 3200 steps == 3 mm
   * 16 steps == 15 microns
   */
  public enum Direction {
    UP(1), NONE(0), DOWN(-1);

    private final int sign;

    Direction(int sign) {
      this.sign = sign;
    }

    public PureLogicFrame micron15multiplyBy(int factor) {
      return new PureLogicFrame(sign * 16 * factor);
    }
  }

  private PureLogicFrame(int step16) {
    super(step16 == 0 ?
            "?%c%c".formatted(13, 10).getBytes(StandardCharsets.UTF_8) :
            "%s %+06d%c%c".formatted(STEP_COMMAND, step16, 13, 10).getBytes(StandardCharsets.UTF_8),
        ByteOrder.LITTLE_ENDIAN);
    microns = (3000 / 200) * (step16 / 16);
  }

  public int getMicrons() {
    return microns;
  }

  public PureLogicFrame inverse() {
    return new PureLogicFrame(-microns * 16 / (3000 / 200));
  }

  @Override
  public String toString() {
    return String.join(SPACE, super.toString(), new String(byteBuffer().array(), StandardCharsets.UTF_8).strip(),
        "[%d %s]".formatted(microns, MetricPrefix.MICRO(METRE)));
  }

  public static Optional<PureLogicFrame> of(StringBuilder buffer) {
    if (buffer.indexOf(ALIVE_COMMAND) == 0) {
      return Optional.of(ALIVE);
    }
    else if (buffer.indexOf(STEP_COMMAND) == 0 && buffer.indexOf(NEW_LINE) > STEP_COMMAND.length()) {
      var substring = buffer.substring(STEP_COMMAND.length(), buffer.indexOf(NEW_LINE)).strip().replaceAll(SPACE, "");
      try {
        return Optional.of(new PureLogicFrame(Integer.parseInt(substring)));
      }
      catch (NumberFormatException e) {
        Logger.getLogger(PureLogicFrame.class.getName()).log(LOG_LEVEL_ERRORS, e, () -> substring);
      }
    }
    return Optional.empty();
  }
}
