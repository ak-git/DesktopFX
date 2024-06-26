package com.ak.appliance.rsce.comm.interceptor;

import com.ak.appliance.rsce.comm.bytes.RsceCommandFrame;
import com.ak.comm.interceptor.AbstractCheckedBytesInterceptor;

/**
 * RSC Energia protocol for Hand Control:
 * <p>
 * <pre>
 *   <b>Address Len Command Par1 Par2 ParN CRC1 CRC2</b>
 * </pre>
 * <br/>
 * Examples:
 * <pre>
 *   1. 0x01(Catch) 0x04(Len) 0x18(Position) 0x64(100%) 0x4b 0xf2
 *   2. 0x00 0x09 0xc7 0xRheo1-Low 0xRheo1-High 0xRheo2-Low 0xRheo2-High 0xInfo-Low 0xInfo-High CRC1 CRC2
 * </pre>
 */
public final class RsceBytesInterceptor extends AbstractCheckedBytesInterceptor<RsceCommandFrame, RsceCommandFrame, RsceCommandFrame.ResponseBuilder> {
  public RsceBytesInterceptor() {
    super("RSC Energia", BaudRate.BR_115200, new RsceCommandFrame.ResponseBuilder(), RsceCommandFrame.off(RsceCommandFrame.Control.ALL));
  }
}