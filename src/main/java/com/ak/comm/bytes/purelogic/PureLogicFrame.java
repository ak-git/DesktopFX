package com.ak.comm.bytes.purelogic;

import com.ak.comm.bytes.BufferFrame;
import tec.uom.se.unit.MetricPrefix;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

import static com.ak.comm.bytes.LogUtils.LOG_LEVEL_ERRORS;
import static com.ak.util.Strings.NEW_LINE;
import static com.ak.util.Strings.SPACE;
import static tec.uom.se.unit.Units.METRE;

public final class PureLogicFrame extends BufferFrame {
  public static final int FRAME_LEN = 15;
  private static final String STEP_COMMAND = "STEP";
  private final int microns;

  /**
   * 1 big step == 16 steps
   * 1 big step == 1.8 degree
   * 200 big steps == 360 degrees == 3 mm
   * 3200 steps == 3 mm
   * 16 steps == 15 microns
   */
  public enum StepCommand {
    MICRON_015(16),
    MICRON_150(MICRON_015.steps * 10),
    MICRON_300(MICRON_150.steps * 2),
    MICRON_1500(MICRON_150.steps * 10);

    private final int steps;

    StepCommand(@Nonnegative int steps) {
      this.steps = steps;
    }

    public PureLogicFrame action(boolean up) {
      int sign = up ? 1 : -1;
      return new PureLogicFrame(steps * sign);
    }
  }

  private PureLogicFrame(int step16) {
    super("%s %+06d%c%c".formatted(STEP_COMMAND, step16, 13, 10).getBytes(StandardCharsets.UTF_8), ByteOrder.LITTLE_ENDIAN);
    microns = (3000 / 200) * (step16 / 16);
  }

  public int getMicrons() {
    return microns;
  }

  @Override
  public String toString() {
    return String.join(SPACE, super.toString(), new String(byteBuffer().array(), StandardCharsets.UTF_8).strip(),
        "[%d %s]".formatted(microns, MetricPrefix.MICRO(METRE)));
  }

  @Nullable
  public static PureLogicFrame of(@Nonnull StringBuilder buffer) {
    if (buffer.indexOf(STEP_COMMAND) == 0 && buffer.indexOf(NEW_LINE) > STEP_COMMAND.length()) {
      var substring = buffer.substring(STEP_COMMAND.length(), buffer.indexOf(NEW_LINE)).strip().replaceAll(SPACE, "");
      try {
        return new PureLogicFrame(Integer.parseInt(substring));
      }
      catch (NumberFormatException e) {
        Logger.getLogger(PureLogicFrame.class.getName()).log(LOG_LEVEL_ERRORS, e, () -> substring);
      }
    }
    return null;
  }
}
