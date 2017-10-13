package com.ak.comm.interceptor.rsce;

import com.ak.comm.bytes.rsce.RsceCommandFrame;
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
public final class RsceBytesInterceptor extends AbstractCheckedBytesInterceptor<RsceCommandFrame.ResponseBuilder, RsceCommandFrame, RsceCommandFrame> {
  public RsceBytesInterceptor() {
    super(BaudRate.BR_115200, RsceCommandFrame.off(RsceCommandFrame.Control.ALL), new RsceCommandFrame.ResponseBuilder());
  }
}