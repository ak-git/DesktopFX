package com.ak.comm.converter;

import io.reactivex.functions.Function;
import org.reactivestreams.Publisher;

public interface Converter<RESPONSE> extends Function<RESPONSE, Publisher<int[]>> {
}
