package com.ak.hardware.nmisr.comm.interceptor;

import java.nio.ByteBuffer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.hardware.nmis.comm.interceptor.NmisBytesInterceptor;
import com.ak.hardware.nmis.comm.interceptor.NmisRequest;
import com.ak.hardware.nmis.comm.interceptor.NmisResponseFrame;
import com.ak.hardware.rsce.comm.interceptor.RsceBytesInterceptor;
import com.ak.hardware.rsce.comm.interceptor.RsceCommandFrame;
import rx.Observable;

public final class NmisRsceBytesInterceptor implements BytesInterceptor<RsceCommandFrame, NmisRequest> {
  private final BytesInterceptor<NmisResponseFrame, NmisRequest> nmis = new NmisBytesInterceptor();
  private final BytesInterceptor<RsceCommandFrame, RsceCommandFrame> rsce = new RsceBytesInterceptor();

  public NmisRsceBytesInterceptor() {
    nmis.getBufferObservable().subscribe(nmisResponseFrame -> {
//      rsce.write(nmisResponseFrame)
    });
  }

  @Nonnull
  @Override
  public String name() {
    return "NMISR";
  }

  @Override
  public int getBaudRate() {
    return nmis.getBaudRate();
  }

  @Override
  public int write(@Nonnull ByteBuffer src) {
    return nmis.write(src);
  }

  @Nullable
  @Override
  public NmisRequest getPingRequest() {
    return nmis.getPingRequest();
  }

  @Nonnull
  @Override
  public ByteBuffer put(@Nonnull NmisRequest nmisRequest) {
    return nmis.put(nmisRequest);
  }

  @Nonnull
  @Override
  public Observable<RsceCommandFrame> getBufferObservable() {
    return rsce.getBufferObservable();
  }

  @Override
  public boolean isOpen() {
    return rsce.isOpen();
  }

  @Override
  public void close() {
    rsce.close();
    nmis.close();
  }
}
