package com.ak.comm.interceptor.nmis;

import javax.inject.Named;

import com.ak.comm.bytes.nmis.NmisRequest;
import com.ak.comm.bytes.nmis.NmisResponseFrame;
import com.ak.comm.interceptor.AbstractCheckedBytesInterceptor;
import org.springframework.context.annotation.Profile;

/**
 * Neuro-Muscular Interface Stand (Test Stand) Format:
 * <p>
 * <pre>
 *   <b>Start  Address Len Data1 Data2 Data3 Data4 Data5 Data6 Data7 Data8 CRC</b>
 *   0x7E   0xХХ    8   0xХХ  0xХХ  0xХХ  0xХХ  0xХХ  0xХХ  0xХХ  0xХХ  0xХХ
 * </pre>
 * <pre>
 *   <b>Test Stand Answer</b>
 *   0x7E (Start)
 *   0x41 (Address Channel 1)
 *   8 (Len)
 *   Velocity [DataL, DataH]
 *   Position [DataL, DataH]
 *   Reserved 0x00
 *   Reserved 0x00
 *   Time Delay [ms]
 *   0xXX (CRC)
 * </pre>
 * <p>
 * Examples:
 * <table border="1">
 * <tr>
 * <th>
 * Code to send OUT
 * </th>
 * <th>
 * Test Stand action
 * </th>
 * <th>
 * Answer from Test Stand
 * </th>
 * </tr>
 * <tr>
 * <td>
 * 0x7E 0x81 0x08 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x07
 * </td>
 * <td>
 * 360 Ohm
 * </td>
 * <td>
 * 0x7E 0x91 0x08 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x17
 * </td>
 * </tr>
 * <tr>
 * <td>
 * 0x7E 0x81 0x08 0x08 0x08 0x08 0x08 0x00 0x00 0x00 0x00 0x27
 * </td>
 * <td>
 * 390 Ohm
 * </td>
 * <td>
 * 0x7E 0x91 0x08 0x08 0x08 0x08 0x08 0x00 0x00 0x00 0x00 0x37
 * </td>
 * </tr>
 * <tr>
 * <td>
 * 0x7E 0x81 0x08 0x00 0x00 0x00 0x00 0x81 0x81 0x81 0x81 0x0B
 * </td>
 * <td>
 * 1 mV, 50 Hz
 * </td>
 * <td>
 * 0x7E 0x91 0x08 0x00 0x00 0x00 0x00 0x81 0x81 0x81 0x81 0x1B
 * </td>
 * </tr>
 * </table>
 */
@Named
@Profile("nmis")
public final class NmisBytesInterceptor extends AbstractCheckedBytesInterceptor<NmisRequest, NmisResponseFrame, NmisResponseFrame.Builder> {
  public NmisBytesInterceptor() {
    super(BaudRate.BR_115200, NmisRequest.Sequence.CATCH_100.build(), new NmisResponseFrame.Builder());
  }
}
