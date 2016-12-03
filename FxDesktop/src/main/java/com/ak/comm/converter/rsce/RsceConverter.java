package com.ak.comm.converter.rsce;

import com.ak.comm.bytes.rsce.RsceCommandFrame;
import com.ak.comm.converter.Converter;
import io.reactivex.Flowable;
import org.reactivestreams.Publisher;

class RsceConverter implements Converter<RsceCommandFrame> {
  @Override
  public Publisher<int[]> apply(RsceCommandFrame frame) {
    if (frame.hasResistance()) {
      return Flowable.just(new int[] {frame.getR1DozenMilliOhms(), frame.getR2DozenMilliOhms()});
    }
    else {
      return Flowable.empty();
    }
  }
}
