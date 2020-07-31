package com.ak.fx.desktop.rcm;

import javax.inject.Named;

import com.ak.comm.GroupService;
import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.converter.LinkedConverter;
import com.ak.comm.converter.rcm.RcmConverter;
import com.ak.comm.converter.rcm.calibration.RcmCalibrationVariable;
import com.ak.comm.interceptor.rcm.RcmBytesInterceptor;
import com.ak.fx.desktop.AbstractViewController;
import org.springframework.context.annotation.Profile;

@Named
@Profile("rcm-calibration")
public final class RcmCalibrationViewController
    extends AbstractViewController<BufferFrame, BufferFrame, RcmCalibrationVariable> {
  public RcmCalibrationViewController() {
    super(
        new GroupService<>(
            RcmBytesInterceptor::new,
            () -> new LinkedConverter<>(new RcmConverter(), RcmCalibrationVariable.class)
        )
    );
  }
}
