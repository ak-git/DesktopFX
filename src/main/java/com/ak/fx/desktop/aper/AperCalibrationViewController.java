package com.ak.fx.desktop.aper;

import javax.inject.Named;

import com.ak.comm.GroupService;
import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.converter.ToIntegerConverter;
import com.ak.comm.converter.aper.calibration.AperCalibrationVariable;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.comm.interceptor.simple.RampBytesInterceptor;
import com.ak.fx.desktop.AbstractViewController;
import org.springframework.context.annotation.Profile;

@Named
@Profile("aper-calibration")
public final class AperCalibrationViewController
    extends AbstractViewController<BufferFrame, BufferFrame, AperCalibrationVariable> {
  public AperCalibrationViewController() {
    super(
        new GroupService<>(
            () -> new RampBytesInterceptor(BytesInterceptor.BaudRate.BR_460800, 25),
            () -> new ToIntegerConverter<>(AperCalibrationVariable.class, 1000)
        )
    );
  }
}
