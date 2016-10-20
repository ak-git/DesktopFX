package com.ak.hardware.rsce.comm.interceptor;

import com.ak.comm.interceptor.AbstractCheckedBytesInterceptor;

/**
 * Format:
 * <pre>
 *   <b>Address Len Command Par1 Par2 ParN CRC1 CRC2</b>
 * </pre>
 * <br/>
 * Examples:
 * <pre>
 *   1. 0x01(Catch) 0x04(Len) 0x18(Position) 0x64(100%) 0x4b 0xf2
 *   2. 0x00 0x09 0xc7 0xRheo1-Low 0xRheo1-High 0xRheo2-Low 0xRheo2-High 0xOpen% 0xRotate% CRC1 CRC2
 * </pre>
 */
public final class RsceBytesInterceptor extends AbstractCheckedBytesInterceptor<RsceCommandFrame.ResponseBuilder, RsceCommandFrame, RsceCommandFrame> {
  public RsceBytesInterceptor() {
    super("RSCE", RsceCommandFrame.off(RsceCommandFrame.Control.ALL), new RsceCommandFrame.ResponseBuilder());
  }
}
