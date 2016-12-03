package com.ak.comm.converter;

import java.util.function.Function;
import java.util.stream.Stream;

public interface Converter<RESPONSE> extends Function<RESPONSE, Stream<int[]>> {
}
