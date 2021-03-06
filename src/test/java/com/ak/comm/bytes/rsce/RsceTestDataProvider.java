package com.ak.comm.bytes.rsce;

import org.testng.annotations.DataProvider;

import static com.ak.comm.bytes.rsce.RsceCommandFrame.Control.ALL;
import static com.ak.comm.bytes.rsce.RsceCommandFrame.Control.CATCH;
import static com.ak.comm.bytes.rsce.RsceCommandFrame.Control.FINGER;
import static com.ak.comm.bytes.rsce.RsceCommandFrame.Control.ROTATE;
import static com.ak.comm.bytes.rsce.RsceCommandFrame.RequestType.EMPTY;
import static com.ak.comm.bytes.rsce.RsceCommandFrame.RequestType.RESERVE;
import static com.ak.comm.bytes.rsce.RsceCommandFrame.RequestType.STATUS_I;
import static com.ak.comm.bytes.rsce.RsceCommandFrame.RequestType.STATUS_I_ANGLE;
import static com.ak.comm.bytes.rsce.RsceCommandFrame.RequestType.STATUS_I_SPEED;
import static com.ak.comm.bytes.rsce.RsceCommandFrame.RequestType.STATUS_I_SPEED_ANGLE;

public class RsceTestDataProvider {
  private RsceTestDataProvider() {
  }

  @DataProvider(name = "simpleRequests")
  public static Object[][] simpleSimpleRequests() {
    return new Object[][] {
        {new byte[] {0x00, 0x03, 0x07, 0x30, (byte) 0xf2}, ALL, RESERVE},

        {new byte[] {0x01, 0x03, 0x00, 0x20, (byte) 0xF0}, CATCH, EMPTY},
        {new byte[] {0x01, 0x03, 0x01, (byte) 0xE1, 0x30}, CATCH, STATUS_I},
        {new byte[] {0x01, 0x03, 0x02, (byte) 0xA1, 0x31}, CATCH, STATUS_I_SPEED},
        {new byte[] {0x01, 0x03, 0x03, 0x60, (byte) 0xF1}, CATCH, STATUS_I_ANGLE},
        {new byte[] {0x01, 0x03, 0x04, 0x21, 0x33}, CATCH, STATUS_I_SPEED_ANGLE},

        {new byte[] {0x02, 0x03, 0x00, (byte) 0xD0, (byte) 0xF0}, FINGER, EMPTY},
        {new byte[] {0x02, 0x03, 0x01, 0x11, 0x30}, FINGER, STATUS_I},
        {new byte[] {0x02, 0x03, 0x02, 0x51, 0x31}, FINGER, STATUS_I_SPEED},
        {new byte[] {0x02, 0x03, 0x03, (byte) 0x90, (byte) 0xF1}, FINGER, STATUS_I_ANGLE},
        {new byte[] {0x02, 0x03, 0x04, (byte) 0xD1, 0x33}, FINGER, STATUS_I_SPEED_ANGLE},

        {new byte[] {0x03, 0x03, 0x00, (byte) 0x81, 0x30}, ROTATE, EMPTY},
        {new byte[] {0x03, 0x03, 0x01, 0x40, (byte) 0xF0}, ROTATE, STATUS_I},
        {new byte[] {0x03, 0x03, 0x02, 0x00, (byte) 0xF1}, ROTATE, STATUS_I_SPEED},
        {new byte[] {0x03, 0x03, 0x03, (byte) 0xC1, 0x31}, ROTATE, STATUS_I_ANGLE},
        {new byte[] {0x03, 0x03, 0x04, (byte) 0x80, (byte) 0xF3}, ROTATE, STATUS_I_SPEED_ANGLE},
    };
  }

  @DataProvider(name = "offRequests")
  public static Object[][] offRequests() {
    return new Object[][] {
        {new byte[] {0x00, 0x03, 0x20, 0x70, (byte) 0xe8}, ALL},
        {new byte[] {0x01, 0x03, 0x20, 0x21, 0x28}, CATCH},
        {new byte[] {0x02, 0x03, 0x20, (byte) 0xD1, 0x28}, FINGER},
        {new byte[] {0x03, 0x03, 0x20, (byte) 0x80, (byte) 0xE8}, ROTATE},
    };
  }

  @DataProvider(name = "preciseRequests")
  public static Object[][] preciseRequests() {
    return new Object[][] {
        {new byte[] {0x01, 0x05, 0x0C, 0x00, 0x00, (byte) 0xD9, 0x0F}, (short) 0},
        {new byte[] {0x01, 0x05, 0x0C, 0x20, 0x4E, 0x40, (byte) 0xFB}, (short) 20000},
        {new byte[] {0x01, 0x05, 0x0C, (byte) 0xE0, (byte) 0xB1, 0x50, (byte) 0xBB}, (short) -20000},
        {new byte[] {0x01, 0x05, 0x0C, (byte) 0xA0, 0x0F, (byte) 0xE1, 0x0B}, (short) 4000},
    };
  }

  @DataProvider(name = "positionCatchRequests", parallel = true)
  public static Object[][] positionCatchRequests() {
    return new Object[][] {
        {new byte[] {0x01, 0x04, 0x18, 0x64, 0x4b, (byte) 0xf2}, (byte) 100},
        {new byte[] {0x01, 0x04, 0x18, 0x00, 0x4a, (byte) 0x19}, (byte) 0},
    };
  }

  @DataProvider(name = "positionRotateRequests", parallel = true)
  public static Object[][] positionRotateRequests() {
    return new Object[][] {
        {new byte[] {0x03, 0x04, 0x18, 0x64, 0x4a, 0x4a}, (byte) 100},
        {new byte[] {0x03, 0x04, 0x18, 0x00, 0x4b, (byte) 0xa1}, (byte) 0},
    };
  }

  @DataProvider(name = "rheo12-info")
  public static Object[][] infoRequests() {
    return new Object[][] {
        {new byte[] {0x00, 0x09, (byte) 0xc7, 0x40, 0x0b, (byte) 0xa0, 0x0b, 0x64, 0x00, (byte) 0xae, 0x55},
            new int[] {2880, 2976}, new int[] {100}},
        {new byte[] {0x00, 0x07, (byte) 0xc7, 0x00, 0x00, 0x00, 0x00, (byte) 0xaf, 0x66},
            new int[] {0, 0}, new int[] {0}},
        {new byte[] {0x00, 0x03, 0x00, 0x71, 0x30},
            new int[] {0, 0}, new int[] {0}}
    };
  }

  @DataProvider(name = "finger")
  public static Object[][] finger() {
    return new Object[][] {
        {new byte[] {0x02, 0x05, 0x0c, 0x20, 0x4e, 0x04, (byte) 0xfb},
            20000},
        {new byte[] {0x02, 0x05, 0x0c, (byte) 0xe0, (byte) 0xb1, 0x14, (byte) 0xbb},
            -20000},
    };
  }

  @DataProvider(name = "invalidRequests", parallel = true)
  public static Object[][] invalidRequests() {
    return new Object[][] {
        //invalid Control byte (first - 0x04)
        {new byte[] {0x04, 0x04, 0x18, 0x64, 0x4b, (byte) 0x3e}},
        //invalid ActionType byte (third - 0xff)
        {new byte[] {0x01, 0x04, (byte) 0xff, 0x64, 0x00, (byte) 0x02}},
        //invalid RequestType byte (third - 0x05)
        {new byte[] {0x01, 0x04, 0x05, 0x64, 0x42, (byte) 0xA2}},
    };
  }
}
